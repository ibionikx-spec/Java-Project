package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.DailyProductionRes;
import com.mangakousei.mangakousei_backend.dto.response.MangakaReportStatsRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.MangakaReportService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mangaka/reports")
@RequiredArgsConstructor
public class MangakaReportController {

    private final MangakaReportService reportService;

    private void requireMangaka() {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        requireMangaka();
        Long mangakaId = SecurityUtils.getCurrentUserId();
        MangakaReportStatsRes result = reportService.getStats(mangakaId);
        return ResponseEntity.ok(ApiResponse.success("OK", result));
    }

    @GetMapping("/daily-production")
    public ResponseEntity<?> getDailyProduction() {
        requireMangaka();
        Long mangakaId = SecurityUtils.getCurrentUserId();
        List<DailyProductionRes> result = reportService.getDailyProduction(mangakaId);
        return ResponseEntity.ok(ApiResponse.success("OK", result));
    }
}