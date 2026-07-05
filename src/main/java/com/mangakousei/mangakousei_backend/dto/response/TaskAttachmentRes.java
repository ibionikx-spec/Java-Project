package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachmentRes {
    private Long attachmentId;
    private Long taskId;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime createdAt;
}
