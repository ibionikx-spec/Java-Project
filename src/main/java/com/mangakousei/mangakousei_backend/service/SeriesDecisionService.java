package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.SeriesDecisionReq;
import com.mangakousei.mangakousei_backend.dto.response.SeriesDecisionRes;
import com.mangakousei.mangakousei_backend.entity.entity.PublicationDecision;
import com.mangakousei.mangakousei_backend.entity.entity.PublicationSchedule;
import com.mangakousei.mangakousei_backend.entity.entity.Series;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.entity.status.SeriesStatus;
import com.mangakousei.mangakousei_backend.entity.type.DecisionType;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.DecisionTypeRepository;
import com.mangakousei.mangakousei_backend.repository.PublicationDecisionRepository;
import com.mangakousei.mangakousei_backend.repository.PublicationScheduleRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesStatusRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeriesDecisionService {

    private static final String DECISION_CANCEL = "cancel";
    private static final String DECISION_CHANGE_SCHEDULE = "change_schedule";
    private static final String DECISION_CONTINUE = "continue";
    private static final List<String> SUPPORTED_DECISIONS = List.of(
            DECISION_CANCEL,
            DECISION_CHANGE_SCHEDULE,
            DECISION_CONTINUE
    );

    private final SeriesRepository seriesRepository;
    private final UserRepository userRepository;
    private final SeriesStatusRepository seriesStatusRepository;
    private final DecisionTypeRepository decisionTypeRepository;
    private final PublicationDecisionRepository decisionRepository;
    private final PublicationScheduleRepository scheduleRepository;
    private final NotificationService notificationService;

    @Transactional
    public SeriesDecisionRes decide(Long seriesId, SeriesDecisionReq req, Long deciderId) {
        String decisionName = normalizeDecision(req.getDecisionType());
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new CustomAppException("Khong tim thay series", HttpStatus.NOT_FOUND));
        User decider = userRepository.findById(deciderId)
                .orElseThrow(() -> new CustomAppException("User not found", HttpStatus.NOT_FOUND));

        if (DECISION_CANCEL.equals(decisionName)) {
            cancelSeries(series);
        } else if (DECISION_CHANGE_SCHEDULE.equals(decisionName)) {
            changeSchedule(series, req);
        }

        PublicationDecision decision = new PublicationDecision();
        decision.setSeries(series);
        decision.setDecider(decider);
        decision.setReason(req.getReason());
        decision.setDecisionType(getOrCreateDecisionType(decisionName));
        PublicationDecision saved = decisionRepository.save(decision);

        notifyStakeholders(series, decisionName, req.getReason());
        return toRes(saved);
    }

    @Transactional(readOnly = true)
    public List<SeriesDecisionRes> getDecisions(Long seriesId) {
        return decisionRepository.findBySeriesSeriesIdOrderByDecidedAtDesc(seriesId)
                .stream()
                .map(this::toRes)
                .toList();
    }

    private String normalizeDecision(String decisionType) {
        String decisionName = decisionType == null ? "" : decisionType.trim().toLowerCase();
        if (!SUPPORTED_DECISIONS.contains(decisionName)) {
            throw new CustomAppException(
                    "Decision khong hop le: cancel, change_schedule hoac continue",
                    HttpStatus.BAD_REQUEST);
        }
        return decisionName;
    }

    private void cancelSeries(Series series) {
        String currentStatus = series.getSeriesStatus() != null
                ? series.getSeriesStatus().getSeriesStatusName() : "";
        if ("cancelled".equalsIgnoreCase(currentStatus)) {
            throw new CustomAppException("Series da bi huy", HttpStatus.BAD_REQUEST);
        }
        SeriesStatus cancelled = seriesStatusRepository.findBySeriesStatusName("cancelled")
                .orElseGet(() -> seriesStatusRepository.save(SeriesStatus.builder()
                        .seriesStatusName("cancelled")
                        .build()));
        series.setSeriesStatus(cancelled);
        seriesRepository.save(series);
    }

    private void changeSchedule(Series series, SeriesDecisionReq req) {
        if (!List.of("weekly", "monthly").contains(req.getScheduleType())) {
            throw new CustomAppException(
                    "scheduleType phai la 'weekly' hoac 'monthly'",
                    HttpStatus.BAD_REQUEST);
        }
        if (req.getDayValue() == null || req.getDayValue() < 1) {
            throw new CustomAppException("dayValue khong hop le", HttpStatus.BAD_REQUEST);
        }
        if ("weekly".equals(req.getScheduleType()) && req.getDayValue() > 7) {
            throw new CustomAppException("weekly can dayValue tu 1 den 7", HttpStatus.BAD_REQUEST);
        }
        if ("monthly".equals(req.getScheduleType()) && req.getDayValue() > 31) {
            throw new CustomAppException("monthly can dayValue tu 1 den 31", HttpStatus.BAD_REQUEST);
        }

        PublicationSchedule schedule = scheduleRepository.findBySeriesSeriesId(series.getSeriesId())
                .orElse(PublicationSchedule.builder().series(series).build());
        schedule.setScheduleType(req.getScheduleType());
        schedule.setDayValue(req.getDayValue());
        scheduleRepository.save(schedule);
    }

    private DecisionType getOrCreateDecisionType(String decisionName) {
        return decisionTypeRepository.findByDecisionTypeName(decisionName)
                .orElseGet(() -> decisionTypeRepository.save(DecisionType.builder()
                        .decisionTypeName(decisionName)
                        .build()));
    }

    private void notifyStakeholders(Series series, String decisionName, String reason) {
        String title = switch (decisionName) {
            case DECISION_CANCEL -> "Series da bi huy";
            case DECISION_CHANGE_SCHEDULE -> "Series thay doi lich xuat ban";
            default -> "Board tiep tuc theo doi series";
        };
        String message = series.getTitle() + ": " + reason;
        if (series.getCreator() != null) {
            notificationService.send(series.getCreator().getUserId(), "SYSTEM", title, message);
        }
        if (series.getEditor() != null) {
            notificationService.send(series.getEditor().getUserId(), "SYSTEM", title, message);
        }
    }

    private SeriesDecisionRes toRes(PublicationDecision decision) {
        Series series = decision.getSeries();
        PublicationSchedule schedule = scheduleRepository.findBySeriesSeriesId(series.getSeriesId()).orElse(null);

        return SeriesDecisionRes.builder()
                .decisionId(decision.getDecisionId())
                .seriesId(series.getSeriesId())
                .seriesTitle(series.getTitle())
                .decisionType(decision.getDecisionType() != null
                        ? decision.getDecisionType().getDecisionTypeName() : null)
                .reason(decision.getReason())
                .seriesStatus(series.getSeriesStatus() != null
                        ? series.getSeriesStatus().getSeriesStatusName() : null)
                .scheduleType(schedule != null ? schedule.getScheduleType() : null)
                .dayValue(schedule != null ? schedule.getDayValue() : null)
                .decidedAt(decision.getDecidedAt())
                .decidedById(decision.getDecider() != null ? decision.getDecider().getUserId() : null)
                .decidedByName(decision.getDecider() != null ? decision.getDecider().getFullName() : null)
                .build();
    }
}
