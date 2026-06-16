package com.mangakousei.mangakousei_backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SeriesSummaryRes {
    private Long seriesId;
    private String title;
    private String description;
    private String createdAt;
    private String approvedAt;
    private String seriesStatus; // tên status
    private List<String> genres; // tên thể loại
}