package com.mangakousei.mangakousei_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationRes {

    private Long   notificationId;
    private String title;
    private String message;
    private String notificationType;
    private boolean isRead;

    @JsonFormat(pattern = "dd/MM/yyyy, HH:mm")
    private LocalDateTime createdAt;
}