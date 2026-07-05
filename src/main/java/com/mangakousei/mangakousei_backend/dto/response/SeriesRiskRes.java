package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeriesRiskRes {
    private Long seriesId;
    private String title;
    private String mangakaName;
    private Integer rankingPosition;
    private Integer previousRankingPosition;
    private Long voteCount;
    private String riskLevel;
    private String reason;
}
