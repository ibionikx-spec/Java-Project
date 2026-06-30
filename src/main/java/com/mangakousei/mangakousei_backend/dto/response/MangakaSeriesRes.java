package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MangakaSeriesRes {
    private Long seriesId;
    private String title;
    private String description;
    private String coverImageUrl;
    private String seriesStatus;
    private String tantouName;
    private String tantouAvatarUrl;
    private int chapterCount;
    private List<String> genres;
    private String approvedAt;

    private String scheduleType;
    private Integer dayValue;
}