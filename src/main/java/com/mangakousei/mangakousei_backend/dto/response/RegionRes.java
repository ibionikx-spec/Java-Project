package com.mangakousei.mangakousei_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RegionRes {
    private Long regionId;
    private BigDecimal x;
    private BigDecimal y;
    private BigDecimal width;
    private BigDecimal height;
    private String regionTypeName;
    private Long regionTypeId;
    private String note;
    private List<TaskRes> tasks;
}
