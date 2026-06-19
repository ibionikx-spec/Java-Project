package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AssistantAssignmentRes {
    private Long assignmentId;

    private Long assistantId;
    private String assistantName;
    private String assistantAvatarUrl;
    private String assistantEmail;

    private Long mangakaId;
    private String mangakaName;
    private String mangakaAvatarUrl;

    private String status;           // pending / active / rejected / inactive
    private LocalDateTime invitedAt;
    private LocalDateTime respondedAt;
}