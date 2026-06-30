package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaskRes {
    private Long taskId;
    private String taskTypeName;
    private String description;
    private LocalDateTime deadline;
    private String taskStatus;

    private Long assignedToId;
    private String assignedToName;
    private String assignedToAvatarUrl;

    private LocalDateTime createdAt;

    private BigDecimal rate;
}