package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdminReviewChapterReq {
    @NotBlank
    @Pattern(regexp = "approved|revision", message = "decision phải là 'approved' hoặc 'revision'")
    private String decision;

    private String note;
}