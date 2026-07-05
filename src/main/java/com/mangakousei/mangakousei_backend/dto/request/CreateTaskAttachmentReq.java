package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskAttachmentReq {
    @NotBlank
    private String fileUrl;

    @NotBlank
    private String fileName;

    private String fileType;
}
