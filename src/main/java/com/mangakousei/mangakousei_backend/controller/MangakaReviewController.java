package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.ReviewSubmissionReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
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

@RestController
@RequestMapping("/api/mangaka/reviews")
@RequiredArgsConstructor
public class MangakaReviewController {

    private final TaskSubmissionService taskSubmissionService;

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingReviews() {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        Long mangakaId = SecurityUtils.getCurrentUserId();
        List<TaskSubmissionRes> result =
                taskSubmissionService.getPendingReviews(mangakaId);
        return ResponseEntity.ok(ApiResponse.success("Pending reviews", result));
    }

    @GetMapping("/tasks/{taskId}/submissions")
    public ResponseEntity<?> getSubmissionsByTask(@PathVariable Long taskId) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        List<TaskSubmissionRes> result =
                taskSubmissionService.getSubmissionsByTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Submissions", result));
    }

    @PatchMapping("/{submissionId}")
    public ResponseEntity<?> reviewSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody ReviewSubmissionReq req) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        Long mangakaId = SecurityUtils.getCurrentUserId();
        TaskSubmissionRes result =
                taskSubmissionService.reviewSubmission(submissionId, req, mangakaId);
        return ResponseEntity.ok(ApiResponse.success("Đã review submission", result));
    }

    private CustomAppException forbidden() {
        return new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
    }
}