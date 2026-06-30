package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateRegionReq {
    @NotNull
    private Long pageId;

    @NotNull private BigDecimal x;
    @NotNull private BigDecimal y;
    @NotNull private BigDecimal width;
    @NotNull private BigDecimal height;

    @NotNull
    private Long regionTypeId;

    private String note;
}