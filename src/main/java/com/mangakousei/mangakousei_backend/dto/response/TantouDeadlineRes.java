package com.mangakousei.mangakousei_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TantouDeadlineRes {

    private Long   deadlineId;
    private String labelType;
    private String label;
    private String timeTag;

    private String title;
    private String author;
    private String series;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dueDate;
}