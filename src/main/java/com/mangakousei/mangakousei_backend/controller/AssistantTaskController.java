package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.SubmitTaskReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.AssistantTaskRes;
import com.mangakousei.mangakousei_backend.dto.response.TaskSubmissionRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.TaskSubmissionService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantTaskController {

    private final TaskSubmissionService taskSubmissionService;

    @GetMapping("/tasks")
    public ResponseEntity<?> getMyTasks(
            @RequestParam(required = false) String status) {
        if (!SecurityUtils.isAssistant()) throw forbidden();
        Long assistantId = SecurityUtils.getCurrentUserId();
        List<AssistantTaskRes> tasks = taskSubmissionService.getMyTasks(assistantId, status);
        return ResponseEntity.ok(ApiResponse.success("Fetched tasks", tasks));
    }

    @PatchMapping("/tasks/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body) {
        if (!SecurityUtils.isAssistant()) throw forbidden();
        Long assistantId = SecurityUtils.getCurrentUserId();
        String newStatus = body.get("status");
        taskSubmissionService.updateTaskStatus(taskId, newStatus, assistantId);
        return ResponseEntity.ok(ApiResponse.success("Task status updated", null));
    }

    @PostMapping("/submissions")
    public ResponseEntity<?> submitWork(@Valid @RequestBody SubmitTaskReq req) {
        if (!SecurityUtils.isAssistant()) throw forbidden();
        Long assistantId = SecurityUtils.getCurrentUserId();
        TaskSubmissionRes result = taskSubmissionService.submitWork(req, assistantId);
        return ResponseEntity.ok(ApiResponse.success("Đã nộp bài", result));
    }

    @GetMapping("/tasks/{taskId}/submissions")
    public ResponseEntity<?> getSubmissions(@PathVariable Long taskId) {
        if (!SecurityUtils.isAssistant()) throw forbidden();
        List<TaskSubmissionRes> submissions =
                taskSubmissionService.getSubmissionsByTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Fetched submissions", submissions));
    }

    private CustomAppException forbidden() {
        return new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
    }
}