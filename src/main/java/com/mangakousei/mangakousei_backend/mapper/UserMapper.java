package com.mangakousei.mangakousei_backend.mapper;

import com.mangakousei.mangakousei_backend.dto.response.UserInfoRes;
import com.mangakousei.mangakousei_backend.entity.entity.User;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserInfoRes toDto(User user) {
        if (user == null) return null;
        
        return UserInfoRes.builder()
            .id(user.getUserId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .avatarUrl(user.getAvatarUrl())
            .roles(user.getRoles().stream()
                .map(r -> r.getRoleName())
                .collect(Collectors.toList())) 
            .build();
    }
}
