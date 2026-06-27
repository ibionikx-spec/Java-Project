package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminCreateUserReq {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank
    @Size(max = 100, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    @NotBlank
    @Pattern(regexp = "TANTOU|MANGAKA|ASSISTANT|ADMIN",
            message = "Role phải là TANTOU, MANGAKA, ASSISTANT hoặc ADMIN")
    private String roleName;
}