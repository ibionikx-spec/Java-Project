package com.mangakousei.mangakousei_backend.dto.request;
 
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
 
@Data
public class SetScheduleReq {
 
    @NotNull
    private Long seriesId;
 
    // "weekly" hoặc "monthly"
    @NotBlank
    private String scheduleType;
 
    // weekly: 1-7 | monthly: 1-31
    @NotNull
    @Min(1) @Max(31)
    private Integer dayValue;
}
