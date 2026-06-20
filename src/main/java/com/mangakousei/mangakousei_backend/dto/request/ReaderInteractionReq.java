package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReaderInteractionReq {
    @NotBlank
    @Size(max = 64)
    private String visitorId;

    @Min(1) @Max(5)
    private Integer rating;
}
