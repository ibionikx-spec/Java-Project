package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileReq {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;
}
