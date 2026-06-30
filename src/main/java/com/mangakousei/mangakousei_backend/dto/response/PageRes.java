package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PageRes {
    private Long pageId;
    private int pageNumber;
    private String fileUrl;
    private String pageStatus;
    private int regionCount;
    private int taskCount;
    private LocalDateTime createdAt;
}