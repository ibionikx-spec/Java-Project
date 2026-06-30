package com.mangakousei.mangakousei_backend.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEventResponse {
    private Long eventId;
    private String type;
    private LocalDate date;
    private String seriesTitle;
    private Long seriesId;      
    private Long chapterId;     
    private Integer chapterNumber;
    private String status;
    private String title;
    private Integer pageFrom;
    private Integer pageTo;
}