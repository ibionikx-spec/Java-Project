package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.AnnotationRes;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.EditorAnnotationService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mangaka")
@RequiredArgsConstructor
public class MangakaAnnotationController {

    private final EditorAnnotationService annotationService;

    @GetMapping("/pages/{pageId}/annotations")
    public ResponseEntity<ApiResponse<List<AnnotationRes>>> getAnnotations(@PathVariable Long pageId) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success(
                "OK",
                annotationService.getForPageAsMangaka(pageId, SecurityUtils.getCurrentUserId())));
    }

    @PatchMapping("/annotations/{annotationId}/resolve")
    public ResponseEntity<ApiResponse<AnnotationRes>> resolve(@PathVariable Long annotationId) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success(
                "Annotation resolved",
                annotationService.resolve(annotationId, SecurityUtils.getCurrentUserId())));
    }

    private CustomAppException forbidden() {
        return new CustomAppException("Khong co quyen", HttpStatus.FORBIDDEN);
    }
}
