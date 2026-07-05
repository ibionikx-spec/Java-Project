package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.CreateAnnotationReq;
import com.mangakousei.mangakousei_backend.dto.request.UpdateAnnotationReq;
import com.mangakousei.mangakousei_backend.dto.response.AnnotationRes;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.EditorAnnotationService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tantou")
@RequiredArgsConstructor
public class TantouAnnotationController {

    private final EditorAnnotationService annotationService;

    @GetMapping("/pages/{pageId}/annotations")
    public ResponseEntity<ApiResponse<List<AnnotationRes>>> getAnnotations(@PathVariable Long pageId) {
        if (!SecurityUtils.isTantou()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success(
                "OK",
                annotationService.getForPageAsTantou(pageId, SecurityUtils.getCurrentUserId())));
    }

    @PostMapping("/annotations")
    public ResponseEntity<ApiResponse<AnnotationRes>> create(@Valid @RequestBody CreateAnnotationReq req) {
        if (!SecurityUtils.isTantou()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success(
                "Annotation created",
                annotationService.create(req, SecurityUtils.getCurrentUserId())));
    }

    @PutMapping("/annotations/{annotationId}")
    public ResponseEntity<ApiResponse<AnnotationRes>> update(
            @PathVariable Long annotationId,
            @Valid @RequestBody UpdateAnnotationReq req
    ) {
        if (!SecurityUtils.isTantou()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success(
                "Annotation updated",
                annotationService.update(annotationId, req, SecurityUtils.getCurrentUserId())));
    }

    @PatchMapping("/annotations/{annotationId}/resolve")
    public ResponseEntity<ApiResponse<AnnotationRes>> resolve(@PathVariable Long annotationId) {
        if (!SecurityUtils.isTantou()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success(
                "Annotation resolved",
                annotationService.resolve(annotationId, SecurityUtils.getCurrentUserId())));
    }

    @DeleteMapping("/annotations/{annotationId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long annotationId) {
        if (!SecurityUtils.isTantou()) throw forbidden();
        annotationService.delete(annotationId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Annotation deleted", null));
    }

    private CustomAppException forbidden() {
        return new CustomAppException("Khong co quyen", HttpStatus.FORBIDDEN);
    }
}
