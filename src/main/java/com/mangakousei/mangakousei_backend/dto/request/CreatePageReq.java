package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreatePageReq {
    @NotNull
    private Long chapterId;

    @NotNull @Positive
    private Integer pageNumber;

    @NotNull
    private String fileUrl;
}