package com.mangakousei.mangakousei_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentRes {
    private Long       paymentId;
    private Long       taskId;
    private String     taskTypeName;
    private String     taskDescription;
    private String     seriesTitle;
    private Integer    chapterNumber;

    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal penaltyPct;
    private long       daysLate;

    private String     paymentMonth;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime taskDeadline;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime submittedAt;
}