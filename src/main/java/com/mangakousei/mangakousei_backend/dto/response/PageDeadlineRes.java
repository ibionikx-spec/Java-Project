
package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PageDeadlineRes {
    private Long deadlineId;
    private Integer pageFrom;
    private Integer pageTo;
    private LocalDate dueDate;
    private String status;           // pending / submitted / approved / revision / late
    private LocalDateTime submittedAt;
    private String setByName;
    private LocalDateTime reviewedAt;
    private String reviewNote;
}