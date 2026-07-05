package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeriesDecisionRes {
    private Long decisionId;
    private Long seriesId;
    private String seriesTitle;
    private String decisionType;
    private String reason;
    private String seriesStatus;
    private String scheduleType;
    private Integer dayValue;
    private LocalDateTime decidedAt;
    private Long decidedById;
    private String decidedByName;
}
