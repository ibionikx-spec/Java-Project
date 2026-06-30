package com.mangakousei.mangakousei_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PersonnelUserRes {
    private Long         userId;
    private String       fullName;
    private String       email;
    private String       avatarUrl;
    private List<String> roles;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime createdAt;
}