package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeriesDecisionReq {
    @NotBlank
    private String decisionType;

    @NotBlank
    private String reason;

    private String scheduleType;

    private Integer dayValue;
}
