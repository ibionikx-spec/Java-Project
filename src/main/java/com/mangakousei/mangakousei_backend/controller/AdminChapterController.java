package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.AdminReviewChapterReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.ChapterRes;
import com.mangakousei.mangakousei_backend.dto.response.PageRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.ChapterPageDeadlineRepository;
import com.mangakousei.mangakousei_backend.service.AdminChapterService;
import com.mangakousei.mangakousei_backend.service.PageService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminChapterController {

    private final AdminChapterService adminChapterService;
    private final ChapterPageDeadlineRepository deadlineRepository;
    private final PageService pageService;

    @GetMapping("/chapters/pending-publish")
    public ResponseEntity<?> getPendingPublishChapters() {
        if (!SecurityUtils.isAdmin()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        List<ChapterRes> chapters = adminChapterService.getPendingPublishChapters();
        return ResponseEntity.ok(ApiResponse.success("Fetched chapters pending publish", chapters));
    }

    @PatchMapping("/chapters/{chapterId}/review")
    public ResponseEntity<?> reviewChapter(@PathVariable Long chapterId,
                                           @Valid @RequestBody AdminReviewChapterReq req) {
        if (!SecurityUtils.isAdmin()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        ChapterRes result = adminChapterService.reviewChapter(chapterId, req);
        return ResponseEntity.ok(ApiResponse.success(
                "approved".equals(req.getDecision())
                        ? "Chapter đã được duyệt đăng"
                        : "Yêu cầu sửa đã được gửi",
                result));
    }

    @GetMapping("/page-deadlines/{deadlineId}/pages")
    public ResponseEntity<?> getPagesForDeadline(@PathVariable Long deadlineId) {
        if (!SecurityUtils.isAdmin()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        var deadline = deadlineRepository.findById(deadlineId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy deadline", HttpStatus.NOT_FOUND));

        Long chapterId = deadline.getChapter().getChapterId();
        int from = deadline.getPageFrom();
        int to = deadline.getPageTo();

        List<PageRes> pages = pageService.getPagesByChapter(chapterId)
                .stream()
                .filter(p -> p.getPageNumber() >= from && p.getPageNumber() <= to)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Fetched pages", pages));
    }
}