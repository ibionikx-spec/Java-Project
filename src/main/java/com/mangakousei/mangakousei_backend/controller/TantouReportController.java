package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.TantouReportStatsRes;
import com.mangakousei.mangakousei_backend.dto.response.TantouSeriesRankRes;
import com.mangakousei.mangakousei_backend.service.TantouReportService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tantou/reports")
@RequiredArgsConstructor
public class TantouReportController {

    private final TantouReportService tantouReportService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<TantouReportStatsRes>> getStats() {
        Long tantouId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "OK", tantouReportService.getStats(tantouId)));
    }

    @GetMapping("/ranking")
    public ResponseEntity<ApiResponse<List<TantouSeriesRankRes>>> getRanking() {
        Long tantouId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "OK", tantouReportService.getRanking(tantouId)));
    }
}