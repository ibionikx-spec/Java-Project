package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.UserInfoRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.TantouMangakaService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
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
}
