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
public class InboxItemRes {
    private String itemType;
    private Long id;
    private String seriesTitle;
    private String content;
    private String submittedBy;
    private LocalDateTime submittedAt;
    private String status;
    private String statusLabel;
}
