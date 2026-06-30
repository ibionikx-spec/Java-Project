package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.AdminDashboardStatsRes;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.TantouSeriesRankRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.AdminDashboardService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsRes>> getStats() {
        if (!SecurityUtils.isAdmin())
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        return ResponseEntity.ok(ApiResponse.success("OK", dashboardService.getStats()));
    }

    @GetMapping("/top-series")
    public ResponseEntity<ApiResponse<List<TantouSeriesRankRes>>> getTopSeries() {
        if (!SecurityUtils.isAdmin())
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        return ResponseEntity.ok(ApiResponse.success("OK", dashboardService.getTopSeries()));
    }
}