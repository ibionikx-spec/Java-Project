package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.SeriesDecisionReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.SeriesDecisionRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.SeriesDecisionService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/series")
@RequiredArgsConstructor
public class AdminSeriesDecisionController {

    private final SeriesDecisionService decisionService;

    @PostMapping("/{seriesId}/decisions")
    public ResponseEntity<ApiResponse<SeriesDecisionRes>> decide(
            @PathVariable Long seriesId,
            @Valid @RequestBody SeriesDecisionReq req
    ) {
        if (!SecurityUtils.isAdmin()) throw forbidden();
        SeriesDecisionRes result = decisionService.decide(seriesId, req, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Decision saved", result));
    }

    @GetMapping("/{seriesId}/decisions")
    public ResponseEntity<ApiResponse<List<SeriesDecisionRes>>> getDecisions(@PathVariable Long seriesId) {
        if (!SecurityUtils.isAdmin()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success("OK", decisionService.getDecisions(seriesId)));
    }

    private CustomAppException forbidden() {
        return new CustomAppException("Khong co quyen", HttpStatus.FORBIDDEN);
    }
}
