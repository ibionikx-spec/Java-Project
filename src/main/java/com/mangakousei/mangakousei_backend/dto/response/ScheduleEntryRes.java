package com.mangakousei.mangakousei_backend.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEntryRes {
    private Long scheduleId;
    private Long seriesId;
    private String seriesTitle;
    private String mangakaName;
    private String scheduleType;  // "weekly" | "monthly"
    private Integer dayValue;
}
