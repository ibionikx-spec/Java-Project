package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.ReviewPageGroupReq;
import com.mangakousei.mangakousei_backend.dto.request.SetPageDeadlineReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.ChapterRes;
import com.mangakousei.mangakousei_backend.dto.response.PageDeadlineRes;
import com.mangakousei.mangakousei_backend.dto.response.PageRes;
import com.mangakousei.mangakousei_backend.entity.entity.ChapterPageDeadline;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.ChapterPageDeadlineRepository;
import com.mangakousei.mangakousei_backend.service.ChapterService;
import com.mangakousei.mangakousei_backend.service.PageService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tantou")
@RequiredArgsConstructor
public class TantouChapterController {

    private final ChapterService chapterService;
    private final PageService pageService;
    private final ChapterPageDeadlineRepository deadlineRepository;

    @GetMapping("/series/{seriesId}/chapters")
    public ResponseEntity<?> getChapters(@PathVariable Long seriesId) {
        if (!SecurityUtils.isTantou()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        List<ChapterRes> chapters = chapterService.getChaptersBySeries(seriesId);
        return ResponseEntity.ok(ApiResponse.success("Fetched chapters", chapters));
    }

    @PostMapping("/chapters/{chapterId}/page-deadlines")
    public ResponseEntity<?> setDeadline(@PathVariable Long chapterId,
                                         @Valid @RequestBody SetPageDeadlineReq req) {
        if (!SecurityUtils.isTantou()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        PageDeadlineRes result = chapterService.setPageDeadline(chapterId, req);
        return ResponseEntity.ok(ApiResponse.success("Deadline đã được set", result));
    }

    @PutMapping("/page-deadlines/{deadlineId}")
    public ResponseEntity<?> updateDeadline(@PathVariable Long deadlineId,
                                            @Valid @RequestBody SetPageDeadlineReq req) {
        if (!SecurityUtils.isTantou()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        PageDeadlineRes result = chapterService.updatePageDeadline(deadlineId, req);
        return ResponseEntity.ok(ApiResponse.success("Deadline đã được cập nhật", result));
    }

    @DeleteMapping("/page-deadlines/{deadlineId}")
    public ResponseEntity<?> deleteDeadline(@PathVariable Long deadlineId) {
        if (!SecurityUtils.isTantou()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        chapterService.deletePageDeadline(deadlineId);
        return ResponseEntity.ok(ApiResponse.success("Deadline đã được xoá", null));
    }

    @GetMapping("/chapters/pending-review")
    public ResponseEntity<?> getPendingReviewChapters() {
        if (!SecurityUtils.isTantou()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        List<ChapterRes> chapters = chapterService.getSubmittedChaptersForTantou();
        return ResponseEntity.ok(ApiResponse.success("Fetched pending review chapters", chapters));
    }

    @PatchMapping("/page-deadlines/{deadlineId}/review")
    public ResponseEntity<?> reviewPageGroup(@PathVariable Long deadlineId,
                                             @Valid @RequestBody ReviewPageGroupReq req) {
        if (!SecurityUtils.isTantou()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        PageDeadlineRes result = chapterService.reviewPageGroup(deadlineId, req);
        return ResponseEntity.ok(ApiResponse.success("Review đã được ghi nhận", result));
    }

    @PatchMapping("/chapters/{chapterId}/submit-to-admin")
    public ResponseEntity<?> submitChapterToAdmin(@PathVariable Long chapterId) {
        if (!SecurityUtils.isTantou()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        ChapterRes result = chapterService.submitChapterToAdmin(chapterId);
        return ResponseEntity.ok(ApiResponse.success("Chapter đã được submit lên admin", result));
    }

    @GetMapping("/page-deadlines/{deadlineId}/pages")
    public ResponseEntity<?> getPagesForDeadline(@PathVariable Long deadlineId) {
        if (!SecurityUtils.isTantou()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        ChapterPageDeadline deadline = deadlineRepository.findById(deadlineId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy deadline", HttpStatus.NOT_FOUND));

        Long chapterId = deadline.getChapter().getChapterId();
        int from = deadline.getPageFrom();
        int to = deadline.getPageTo();

        List<PageRes> pages = pageService.getPagesByChapter(chapterId)
                .stream()
                .filter(p -> p.getPageNumber() >= from && p.getPageNumber() <= to)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Fetched pages for deadline", pages));
    }
}