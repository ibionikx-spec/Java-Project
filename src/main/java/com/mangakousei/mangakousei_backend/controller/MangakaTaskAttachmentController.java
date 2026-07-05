package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.CreateTaskAttachmentReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.TaskAttachmentRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.TaskAttachmentService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mangaka/tasks")
@RequiredArgsConstructor
public class MangakaTaskAttachmentController {

    private final TaskAttachmentService attachmentService;

    @GetMapping("/{taskId}/attachments")
    public ResponseEntity<ApiResponse<List<TaskAttachmentRes>>> getAttachments(@PathVariable Long taskId) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success(
                "OK",
                attachmentService.getForMangaka(taskId, SecurityUtils.getCurrentUserId())));
    }

    @PostMapping("/{taskId}/attachments")
    public ResponseEntity<ApiResponse<TaskAttachmentRes>> create(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateTaskAttachmentReq req
    ) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success(
                "Attachment created",
                attachmentService.create(taskId, req, SecurityUtils.getCurrentUserId())));
    }

    @DeleteMapping("/{taskId}/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId
    ) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        attachmentService.delete(taskId, attachmentId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted", null));
    }

    private CustomAppException forbidden() {
        return new CustomAppException("Khong co quyen", HttpStatus.FORBIDDEN);
    }
}
