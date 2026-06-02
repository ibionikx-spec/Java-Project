package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginReq {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    private boolean isRememberMe;
}