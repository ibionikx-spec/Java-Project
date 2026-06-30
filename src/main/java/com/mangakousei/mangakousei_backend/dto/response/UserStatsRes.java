package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserStatsRes {
    private Long createdSeriesCount;
    private Long editedSeriesCount;
    private Long manuscriptCount;
    private Long totalPagesCreated;
    private Long totalChaptersCreated;
}
