package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.RespondInvitationReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.AssistantAssignmentRes;
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
@RequestMapping("/api/assistant/invitations")
@RequiredArgsConstructor
public class AssistantInvitationController {

    private final MangakaAssistantService assistantService;

    @GetMapping
    public ResponseEntity<?> getMyInvitations() {
        if (!SecurityUtils.isAssistant()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        Long assistantId = SecurityUtils.getCurrentUserId();
        List<AssistantAssignmentRes> result =
                assistantService.getMyInvitations(assistantId);
        return ResponseEntity.ok(ApiResponse.success("Invitations", result));
    }

    @GetMapping("/count")
    public ResponseEntity<?> countPending() {
        Long assistantId = SecurityUtils.getCurrentUserId();
        long count = assistantService.countPendingInvitations(assistantId);
        return ResponseEntity.ok(ApiResponse.success("Count", count));
    }

    @PatchMapping("/{assignmentId}/respond")
    public ResponseEntity<?> respond(
            @PathVariable Long assignmentId,
            @Valid @RequestBody RespondInvitationReq req) {
        if (!SecurityUtils.isAssistant()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        Long assistantId = SecurityUtils.getCurrentUserId();
        AssistantAssignmentRes result =
                assistantService.respondToInvitation(assignmentId, req, assistantId);
        return ResponseEntity.ok(ApiResponse.success("Đã phản hồi lời mời", result));
    }
}