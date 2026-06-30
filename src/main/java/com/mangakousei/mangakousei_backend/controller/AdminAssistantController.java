package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.MangakaAssistantService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/assistant-assignments")
@RequiredArgsConstructor
public class AdminAssistantController {

    private final MangakaAssistantService assistantService;

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<?> deactivate(@PathVariable Long assignmentId) {
        if (!SecurityUtils.isAdmin()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        Long adminId = SecurityUtils.getCurrentUserId();
        assistantService.deactivate(assignmentId, adminId);
        return ResponseEntity.ok(ApiResponse.success("Đã ngắt quan hệ", null));
    }
}