package com.mangakousei.mangakousei_backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardStatsRes {
    private long totalSeries;
    private long totalMangaka;
    private long totalTantou;
    private long totalAssistant;

    private long pendingAdminProposals;
    private long approvedProposals;

    private long pendingPublishChapters;
    private long publishedChapters;
}