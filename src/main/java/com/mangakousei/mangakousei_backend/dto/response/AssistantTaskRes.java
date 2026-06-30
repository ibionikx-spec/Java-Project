package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AssistantTaskRes {
    private Long taskId;
    private String taskTypeName;
    private String description;
    private LocalDateTime deadline;
    private String taskStatus;

    private String assignedByName;
    private String assignedByAvatarUrl;

    private Long regionId;
    private BigDecimal regionX;
    private BigDecimal regionY;
    private BigDecimal regionWidth;
    private BigDecimal regionHeight;
    private String regionTypeName;
    private String regionNote;

    private Long pageId;
    private Integer pageNumber;
    private String pageFileUrl;

    private Long chapterId;
    private Integer chapterNumber;
    private String chapterTitle;

    private Long seriesId;
    private String seriesTitle;

    private int submissionCount;
    private String latestSubmissionStatus;

    private LocalDateTime createdAt;
}