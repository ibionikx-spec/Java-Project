package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.UpdateSeriesReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.MangakaSeriesRes;
import com.mangakousei.mangakousei_backend.dto.response.UserInfoRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.MangakaSeriesService;
import com.mangakousei.mangakousei_backend.service.TantouMangakaService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mangaka")
@RequiredArgsConstructor
public class TantouMangakaController {

    private final TantouMangakaService tantouMangakaService;
    private final MangakaSeriesService mangakaSeriesService;

    @GetMapping("/assigned-tantous")
    public ResponseEntity<ApiResponse<List<UserInfoRes>>> getMyAssignedTantous() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        List<UserInfoRes> tantous = tantouMangakaService.getActiveTantousForMangaka(currentUserId);
        
        return ResponseEntity.ok(ApiResponse.success("", tantous));
    }
    
    @GetMapping("/{mangakaId}/assigned-tantous")
    public ResponseEntity<ApiResponse<List<UserInfoRes>>> getAssignedTantousForMangaka(
            @PathVariable Long mangakaId) {
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        if (!SecurityUtils.isAdmin() && !currentUserId.equals(mangakaId)) {
            throw new CustomAppException(
                "You don't have permission to view this data",
                HttpStatus.FORBIDDEN
            );
        }
        
        List<UserInfoRes> tantous = tantouMangakaService.getActiveTantousForMangaka(mangakaId);
        
        return ResponseEntity.ok(ApiResponse.success("", tantous));
    }

    @GetMapping("/series")
    public ResponseEntity<?> getMySeries() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<MangakaSeriesRes> series = mangakaSeriesService.getSeriesByMangaka(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Fetched series", series));
    }

    @GetMapping("/series/{seriesId}")
    public ResponseEntity<?> getSeriesDetail(@PathVariable Long seriesId) {
        Long mangakaId = SecurityUtils.getCurrentUserId();
        MangakaSeriesRes result = mangakaSeriesService.getSeriesDetail(seriesId, mangakaId);
        return ResponseEntity.ok(ApiResponse.success("Fetched series detail", result));
    }

    @PutMapping("/series/{seriesId}")
    public ResponseEntity<?> updateSeries(
            @PathVariable Long seriesId,
            @Valid @RequestBody UpdateSeriesReq req) {
        Long mangakaId = SecurityUtils.getCurrentUserId();
        MangakaSeriesRes result = mangakaSeriesService.updateSeries(seriesId, mangakaId, req);
        return ResponseEntity.ok(ApiResponse.success("Series đã được cập nhật", result));
    }
}
