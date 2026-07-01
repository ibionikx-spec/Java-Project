package com.mangakousei.mangakousei_backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConversationRes {
    private Long conversationId;

    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatarUrl;
    private String otherUserRole;

    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
}