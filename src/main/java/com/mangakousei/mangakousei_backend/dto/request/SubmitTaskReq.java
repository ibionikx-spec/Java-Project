package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitTaskReq {
    @NotNull
    private Long taskId;

    @NotBlank
    private String fileUrl;

    private String note;
}