package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaskSubmissionRes {
    private Long submissionId;
    private String fileUrl;
    private String note;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    private Long submittedById;
    private String submittedByName;
    private String submittedByAvatarUrl;

    private Long reviewedById;
    private String reviewedByName;

    private Long taskId;
    private String taskTypeName;
    private String taskDescription;

    private Long pageId;
    private Integer pageNumber;
}