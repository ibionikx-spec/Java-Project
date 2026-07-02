package com.mangakousei.mangakousei_backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MangakaReportStatsRes {
    private long totalSeries;
    private long totalPages;
    private long totalDeadlines;
    private long submittedDeadlines;
    private long pendingDeadlines;
    private double completionRate;
}