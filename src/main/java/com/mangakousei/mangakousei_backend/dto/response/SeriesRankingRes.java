package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeriesRankingRes {
    private Long seriesId;
    private String title;
    private String mangakaName;
    private Integer rankingPosition;
    private Integer previousRankingPosition;
    private Long voteCount;
    private BigDecimal surveyScore;
    private Long salesCount;
    private Long commentCount;
    private LocalDateTime calculatedAt;
}
