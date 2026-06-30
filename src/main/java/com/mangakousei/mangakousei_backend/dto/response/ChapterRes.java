package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChapterRes {
    private Long chapterId;
    private int chapterNumber;
    private String title;
    private String chapterStatus;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private List<PageDeadlineRes> pageDeadlines;
    private long totalDeadlines;
    private long submittedDeadlines;

    private Long seriesId;
    private String seriesTitle;
    private String mangakaName;
    private String mangakaAvatarUrl;

    private String adminNote;
}
