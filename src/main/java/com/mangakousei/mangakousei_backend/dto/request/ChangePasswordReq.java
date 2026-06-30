package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordReq {

    @NotBlank(message = "Current password cannot be blank")
    private String currentPassword;

    @NotBlank(message = "New password cannot be blank")
    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
    private String newPassword;
}
