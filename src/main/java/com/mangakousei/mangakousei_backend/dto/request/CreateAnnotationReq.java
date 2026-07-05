package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnnotationReq {
    @NotNull
    private Long pageId;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal x;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal y;

    @NotNull
    @DecimalMin("0.1")
    @DecimalMax("100.0")
    private BigDecimal width;

    @NotNull
    @DecimalMin("0.1")
    @DecimalMax("100.0")
    private BigDecimal height;

    @NotNull
    private Long annotationTypeId;

    @NotBlank
    private String commentText;
}
