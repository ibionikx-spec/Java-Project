package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileReq {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;

    @Pattern(regexp = "^$|^[0-9+\\-\\s]{8,15}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Size(max = 500, message = "Giới thiệu tối đa 500 ký tự")
    private String bio;
}
