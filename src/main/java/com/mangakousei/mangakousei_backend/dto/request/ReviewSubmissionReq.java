package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewSubmissionReq {
    @NotBlank
    private String decision;

    private String feedback;
}