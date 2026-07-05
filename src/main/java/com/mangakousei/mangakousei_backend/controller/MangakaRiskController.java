package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.SeriesRiskRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.SeriesRiskService;
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
public class MangakaRiskController {

    private final SeriesRiskService riskService;

    @GetMapping("/risks")
    public ResponseEntity<ApiResponse<List<SeriesRiskRes>>> getMyRisks() {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Khong co quyen", HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(ApiResponse.success(
                "OK",
                riskService.getLatestRisksForMangaka(SecurityUtils.getCurrentUserId())));
    }
}
