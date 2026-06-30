package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.InviteAssistantReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.AssistantAssignmentRes;
import com.mangakousei.mangakousei_backend.dto.response.AssistantSearchRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.MangakaAssistantService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mangaka/assistants")
@RequiredArgsConstructor
public class MangakaAssistantController {

    private final MangakaAssistantService assistantService;

    @GetMapping("/search")
    public ResponseEntity<?> searchAssistants(
            @RequestParam String keyword) {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        Long mangakaId = SecurityUtils.getCurrentUserId();
        List<AssistantSearchRes> result =
                assistantService.searchAssistants(keyword, mangakaId);
        return ResponseEntity.ok(ApiResponse.success("Search results", result));
    }

    @PostMapping("/invite")
    public ResponseEntity<?> invite(@Valid @RequestBody InviteAssistantReq req) {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        Long mangakaId = SecurityUtils.getCurrentUserId();
        AssistantAssignmentRes result =
                assistantService.inviteAssistant(req, mangakaId);
        return ResponseEntity.ok(ApiResponse.success("Lời mời đã được gửi", result));
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActive() {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        Long mangakaId = SecurityUtils.getCurrentUserId();
        List<AssistantAssignmentRes> result =
                assistantService.getActiveAssistants(mangakaId);
        return ResponseEntity.ok(ApiResponse.success("Active assistants", result));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPending() {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        Long mangakaId = SecurityUtils.getCurrentUserId();
        List<AssistantAssignmentRes> result =
                assistantService.getPendingInvitations(mangakaId);
        return ResponseEntity.ok(ApiResponse.success("Pending invitations", result));
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<?> deactivate(@PathVariable Long assignmentId) {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        Long mangakaId = SecurityUtils.getCurrentUserId();
        assistantService.deactivate(assignmentId, mangakaId);
        return ResponseEntity.ok(ApiResponse.success("Đã ngắt quan hệ", null));
    }
}