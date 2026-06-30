package com.mangakousei.mangakousei_backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TantouReportStatsRes {
    private long totalSeries;
    private long publishedChapters;
    private long pendingReviewChapters;
    private long totalDeadlines;
    private long overdueDeadlines;
    private long submittedDeadlines;
}