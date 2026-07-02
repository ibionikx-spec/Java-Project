package com.mangakousei.mangakousei_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DailyProductionRes {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String dayLabel;
    private long submittedCount;
}