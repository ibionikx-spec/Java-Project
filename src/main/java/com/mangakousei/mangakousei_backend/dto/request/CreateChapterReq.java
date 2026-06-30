package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateChapterReq {
    @NotNull
    private Long seriesId;

    @NotNull @Positive
    private Integer chapterNumber;

    private String title;
}