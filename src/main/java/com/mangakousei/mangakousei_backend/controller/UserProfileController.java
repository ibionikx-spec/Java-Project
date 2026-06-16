package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.ChangePasswordReq;
import com.mangakousei.mangakousei_backend.dto.request.UpdateProfileReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.UserFullProfileRes;
import com.mangakousei.mangakousei_backend.dto.response.UserInfoRes;
import com.mangakousei.mangakousei_backend.dto.response.UserStatsRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoRes>> updateMyProfile(
            @RequestBody @Valid UpdateProfileReq request
    ) {
        UserInfoRes profile = userProfileService.updateMyProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(
            @RequestBody @Valid ChangePasswordReq request
    ) {
        userProfileService.changeMyPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserInfoRes>> getUserById(@PathVariable Long userId) {
        UserInfoRes profile = userProfileService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", profile));
    }
    @GetMapping("/{userId}/stats")
    public ResponseEntity<ApiResponse<UserStatsRes>> getUserStat(@PathVariable Long userId){
        UserStatsRes stats = userProfileService.getUserStats(userId);
        return ResponseEntity.ok(ApiResponse.success("User stats fetched successfully", stats));
    }
    @PostMapping("/me/avatar")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomAppException("File cannot be blank", HttpStatus.BAD_REQUEST);
        }
        String avatarUrl = userProfileService.updateAvatar(file);
        return ResponseEntity.ok(ApiResponse.success("Avatar upload successfullt", Map.of("avatarUrl", avatarUrl)));
    }
    @GetMapping("/{userId}/full-profile")
        public ResponseEntity<ApiResponse<UserFullProfileRes>> getUserFullProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Success", userProfileService.getUserFullProfile(userId)));
    }
}