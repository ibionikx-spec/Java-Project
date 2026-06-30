package com.mangakousei.mangakousei_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ActivityLogRes {

    private Long   logId;
    private String actionType;
    private String category;
    private String detail;

    private String entityType;
    private Long   entityId;

    private Long seriesId;
    private Long chapterId;

    private Long   userId;
    private String userFullName;
    private String userAvatarUrl;
    private String userRole;

    @JsonFormat(pattern = "dd/MM/yyyy, HH:mm")
    private LocalDateTime createdAt;
}