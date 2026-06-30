package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class UpdateSeriesReq {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private String coverImageUrl;

    @NotNull
    private List<Long> genreIds;
}