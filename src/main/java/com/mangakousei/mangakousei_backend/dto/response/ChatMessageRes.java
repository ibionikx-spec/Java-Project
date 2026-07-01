package com.mangakousei.mangakousei_backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessageRes {
    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatarUrl;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
}