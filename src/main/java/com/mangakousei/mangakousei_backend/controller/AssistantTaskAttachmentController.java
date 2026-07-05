package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.TaskAttachmentRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.TaskAttachmentService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assistant/tasks")
@RequiredArgsConstructor
public class AssistantTaskAttachmentController {

    private final TaskAttachmentService attachmentService;

    @GetMapping("/{taskId}/attachments")
    public ResponseEntity<ApiResponse<List<TaskAttachmentRes>>> getAttachments(@PathVariable Long taskId) {
        if (!SecurityUtils.isAssistant()) {
            throw new CustomAppException("Khong co quyen", HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(ApiResponse.success(
                "OK",
                attachmentService.getForAssistant(taskId, SecurityUtils.getCurrentUserId())));
    }
}
