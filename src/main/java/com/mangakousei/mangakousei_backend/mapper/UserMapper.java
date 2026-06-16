package com.mangakousei.mangakousei_backend.mapper;

import com.mangakousei.mangakousei_backend.dto.response.SeriesSummaryRes;
import com.mangakousei.mangakousei_backend.dto.response.UserFullProfileRes;
import com.mangakousei.mangakousei_backend.dto.response.UserInfoRes;
import com.mangakousei.mangakousei_backend.entity.entity.Genre;
import com.mangakousei.mangakousei_backend.entity.entity.Series;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.entity.system.Role;

import java.util.List;
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
            .editedSeries((long) user.getEditedSeries().size())
            .createdSeries((long) user.getCreatedSeries().size())
            .build();
    }

    public UserFullProfileRes toFullProfileDto(User user) {
    return UserFullProfileRes.builder()
        .id(user.getUserId())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .roles(user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList()))
        .avatarUrl(user.getAvatarUrl())
        .createdSeries(mapSeriesList(user.getCreatedSeries()))
        .editedSeries(mapSeriesList(user.getEditedSeries()))
        .build();
    }

    private List< SeriesSummaryRes>mapSeriesList(List<Series> seriesList) {
        if (seriesList == null) return List.of();
        return seriesList.stream()
            .map(s -> SeriesSummaryRes.builder()    
                .seriesId(s.getSeriesId())
                .title(s.getTitle())
                .description(s.getDescription())
                .createdAt(s.getCreatedAt().toString())
                .approvedAt(s.getApprovedAt() != null ? s.getApprovedAt().toString() : null)
                .seriesStatus(s.getSeriesStatus().getSeriesStatusName())
                .genres(s.getGenres().stream().map(Genre::getGenreName).collect(Collectors.toList()))
                .build())
            .collect(Collectors.toList());
    }
}
