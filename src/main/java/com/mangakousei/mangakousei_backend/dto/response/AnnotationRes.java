package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationRes {
    private Long annotationId;
    private Long pageId;
    private BigDecimal x;
    private BigDecimal y;
    private BigDecimal width;
    private BigDecimal height;
    private String commentText;
    private Long annotationTypeId;
    private String annotationTypeName;
    private String status;
    private Long editorId;
    private String editorName;
    private LocalDateTime createdAt;
}
