package com.mangakousei.mangakousei_backend.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PublicSeriesRes {
    private Long seriesId;
    private String title;
    private String description;
    private String coverImageUrl;
    private String mangakaName;
    private String seriesStatus;
    private List<String> genres;
    private int chapterCount;
    private long views;
    private long voteCount;
    private double rating;
    private LocalDateTime approvedAt;
    private List<PublicChapterRes> chapters;
}
