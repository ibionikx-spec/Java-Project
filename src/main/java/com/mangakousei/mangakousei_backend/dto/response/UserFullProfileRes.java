package com.mangakousei.mangakousei_backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserFullProfileRes {
    private Long id;
    private String fullName;
    private String email;
    private List<String> roles;
    private String avatarUrl;
    private List<SeriesSummaryRes> createdSeries;
    private List<SeriesSummaryRes> editedSeries;
}

