package com.mangakousei.mangakousei_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginReq {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @JsonProperty("isRememberMe")
    private boolean isRememberMe;
}
