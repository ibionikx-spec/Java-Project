
package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TantouSeriesRes {
    private Long seriesId;
    private String title;
    private String coverImageUrl;
    private String seriesStatus;
    private String mangakaName;
    private String mangakaAvatarUrl;
    private int chapterCount;
    private List<String> genres;
    private String approvedAt;

    private String scheduleType;
    private Integer dayValue;

    private long totalPageDeadlines;
    private long submittedPageDeadlines;
}