package com.mangakousei.mangakousei_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoRes {
    private Long id;
    private String fullName;
    private String email;
    private List<String> roles;
    private String avatarUrl;
    private Long editedSeries;
    private Long createdSeries;
    private String phone;
    private String bio;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDateTime passwordChangedAt;
}