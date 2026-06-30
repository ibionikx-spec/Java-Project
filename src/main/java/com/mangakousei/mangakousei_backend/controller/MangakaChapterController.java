package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.CreateChapterReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.ChapterRes;
import com.mangakousei.mangakousei_backend.dto.response.PageDeadlineRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.ChapterService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mangaka")
@RequiredArgsConstructor
public class MangakaChapterController {

    private final ChapterService chapterService;

    @GetMapping("/series/{seriesId}/chapters")
    public ResponseEntity<?> getChapters(@PathVariable Long seriesId) {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        List<ChapterRes> chapters = chapterService.getChaptersBySeries(seriesId);
        return ResponseEntity.ok(ApiResponse.success("Fetched chapters", chapters));
    }

    @PostMapping("/chapters")
    public ResponseEntity<?> createChapter(@Valid @RequestBody CreateChapterReq req) {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        ChapterRes chapter = chapterService.createChapter(req);
        return ResponseEntity.ok(ApiResponse.success("Chapter created successfully", chapter));
    }

    @PatchMapping("/page-deadlines/{deadlineId}/submit")
    public ResponseEntity<?> submitPageGroup(@PathVariable Long deadlineId) {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        PageDeadlineRes result = chapterService.submitPageGroup(deadlineId);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu nộp trang", result));
    }
}