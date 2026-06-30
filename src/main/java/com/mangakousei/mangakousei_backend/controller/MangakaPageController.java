package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.CreatePageReq;
import com.mangakousei.mangakousei_backend.dto.request.UpdatePageReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.PageRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.PageService;
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
public class MangakaPageController {

    private final PageService pageService;

    @GetMapping("/chapters/{chapterId}/pages")
    public ResponseEntity<?> getPages(@PathVariable Long chapterId) {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        List<PageRes> pages = pageService.getPagesByChapter(chapterId);
        return ResponseEntity.ok(ApiResponse.success("Fetched pages", pages));
    }

    @PostMapping("/pages")
    public ResponseEntity<?> createPage(@Valid @RequestBody CreatePageReq req) {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        PageRes result = pageService.createPage(req);
        return ResponseEntity.ok(ApiResponse.success("Page created", result));
    }

    @PutMapping("/pages/{pageId}")
    public ResponseEntity<?> updatePage(@PathVariable Long pageId,
                                        @Valid @RequestBody UpdatePageReq req) {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        PageRes result = pageService.updatePage(pageId, req);
        return ResponseEntity.ok(ApiResponse.success("Page updated", result));
    }

    @DeleteMapping("/pages/{pageId}")
    public ResponseEntity<?> deletePage(@PathVariable Long pageId) {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        pageService.deletePage(pageId);
        return ResponseEntity.ok(ApiResponse.success("Page deleted", null));
    }
}