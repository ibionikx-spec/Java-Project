package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.LogContext;
import com.mangakousei.mangakousei_backend.dto.response.IncomeMonthRes;
import com.mangakousei.mangakousei_backend.dto.response.PaymentRes;
import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.entity.type.ActionType;
import com.mangakousei.mangakousei_backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal PENALTY_PER_DAY = new BigDecimal("0.05");
    private static final BigDecimal MAX_PENALTY     = new BigDecimal("0.50");

    private final PaymentRepository       paymentRepository;
    private final ActivityLogService      activityLogService;
    private final NotificationService     notificationService;

    @Transactional
    public void createPaymentOnApprove(Task task, TaskSubmission submission, Long mangakaId) {
        if (task.getRate() == null || task.getRate().compareTo(BigDecimal.ZERO) <= 0) {
            log.info("[Payment] Task {} không có rate, bỏ qua tạo payment", task.getTaskId());
            return;
        }

        if (paymentRepository.findByTaskTaskId(task.getTaskId()).isPresent()) {
            log.warn("[Payment] Task {} đã có payment, bỏ qua", task.getTaskId());
            return;
        }

        LocalDateTime deadline    = task.getDeadline();
        LocalDateTime submittedAt = submission.getSubmittedAt();
        long daysLate = 0;
        BigDecimal penaltyPct = BigDecimal.ZERO;

        if (submittedAt != null && submittedAt.isAfter(deadline)) {
            daysLate   = ChronoUnit.DAYS.between(deadline, submittedAt);
            penaltyPct = PENALTY_PER_DAY.multiply(BigDecimal.valueOf(daysLate))
                    .min(MAX_PENALTY);
        }

        BigDecimal rate       = task.getRate();
        BigDecimal penaltyAmt = rate.multiply(penaltyPct).setScale(0, RoundingMode.HALF_UP);
        BigDecimal amount     = rate.subtract(penaltyAmt).max(BigDecimal.ZERO);

        String paymentMonth = YearMonth.from(LocalDateTime.now())
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));

        Payment payment = Payment.builder()
                .assistant(task.getAssignedTo())
                .task(task)
                .amount(amount)
                .paymentMonth(paymentMonth)
                .build();

        paymentRepository.save(payment);

        activityLogService.log(mangakaId, LogContext.builder()
                .actionType(ActionType.CREATE_PAYMENT)
                .detail("Tạo thanh toán " + formatVnd(amount)
                        + " cho Assistant " + task.getAssignedTo().getFullName()
                        + (daysLate > 0 ? " (trễ " + daysLate + " ngày, phạt "
                                          + penaltyPct.multiply(BigDecimal.valueOf(100)).intValue() + "%)" : ""))
                .entityType("PAYMENT")
                .entityId(payment.getPaymentId())
                .build());

        String penaltyNote = daysLate > 0
                ? " (đã trừ " + penaltyPct.multiply(BigDecimal.valueOf(100)).intValue()
                  + "% do trễ " + daysLate + " ngày)"
                : "";
        notificationService.send(task.getAssignedTo().getUserId(), "SYSTEM",
                "💰 Thanh toán task được ghi nhận",
                "Bài nộp của bạn đã được duyệt. Số tiền " + formatVnd(amount)
                        + penaltyNote + " sẽ được thanh toán vào kỳ lương tháng "
                        + paymentMonth + ".");
    }

    @Transactional(readOnly = true)
    public IncomeMonthRes getMyIncome(Long assistantId, String month) {
        if (month == null || month.isBlank()) {
            month = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        List<Payment> payments = paymentRepository
                .findByAssistantUserIdAndPaymentMonthOrderByCreatedAtDesc(assistantId, month);

        BigDecimal totalAmount = paymentRepository
                .sumAmountByAssistantAndMonth(assistantId, month);

        YearMonth ym       = YearMonth.parse(month);
        String    prevMonth = ym.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        BigDecimal prevAmt  = paymentRepository.sumPrevMonth(assistantId, prevMonth);

        String monthLabel = "Tháng " + ym.getMonthValue() + ", " + ym.getYear();

        return IncomeMonthRes.builder()
                .month(month)
                .monthLabel(monthLabel)
                .totalAmount(totalAmount)
                .prevMonthAmount(prevAmt)
                .taskCount(payments.size())
                .payments(payments.stream().map(this::toRes).collect(Collectors.toList()))
                .build();
    }

    private PaymentRes toRes(Payment p) {
        Task    task    = p.getTask();
        Page    page    = task.getRegion() != null ? task.getRegion().getPage() : null;
        Chapter chapter = page != null ? page.getChapter() : null;
        Series  series  = chapter != null ? chapter.getSeries() : null;

        BigDecimal rate       = task.getRate() != null ? task.getRate() : BigDecimal.ZERO;
        BigDecimal amount     = p.getAmount();
        BigDecimal penaltyAmt = rate.subtract(amount);
        BigDecimal penaltyPct = rate.compareTo(BigDecimal.ZERO) > 0
                ? penaltyAmt.divide(rate, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        LocalDateTime submittedAt = task.getTaskSubmissions().stream()
                .filter(s -> "approved".equals(s.getTaskSubmissionStatus() != null
                        ? s.getTaskSubmissionStatus().getTaskSubmissionStatusName() : ""))
                .findFirst()
                .map(TaskSubmission::getSubmittedAt)
                .orElse(null);

        long daysLate = 0;
        if (submittedAt != null && submittedAt.isAfter(task.getDeadline())) {
            daysLate = ChronoUnit.DAYS.between(task.getDeadline(), submittedAt);
        }

        return PaymentRes.builder()
                .paymentId(p.getPaymentId())
                .taskId(task.getTaskId())
                .taskTypeName(task.getTaskType() != null
                        ? task.getTaskType().getTaskTypeName() : null)
                .taskDescription(task.getDescription())
                .seriesTitle(series != null ? series.getTitle() : null)
                .chapterNumber(chapter != null ? chapter.getChapterNumber() : null)
                .rate(rate)
                .amount(amount)
                .penaltyPct(penaltyPct.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP))
                .daysLate(daysLate)
                .paymentMonth(p.getPaymentMonth())
                .createdAt(p.getCreatedAt())
                .taskDeadline(task.getDeadline())
                .submittedAt(submittedAt)
                .build();
    }

    private String formatVnd(BigDecimal amount) {
        return String.format("%,.0f VND", amount);
    }
}