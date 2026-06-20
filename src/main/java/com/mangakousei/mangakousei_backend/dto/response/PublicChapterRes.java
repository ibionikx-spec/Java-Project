package com.mangakousei.mangakousei_backend.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PublicChapterRes {
    private Long chapterId;
    private int chapterNumber;
    private String title;
    private String chapterStatus;
    private LocalDateTime createdAt;
    private List<PublicPageRes> pages;
}
