package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.ImportVoteBatchReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.ReaderVoteBatchRes;
import com.mangakousei.mangakousei_backend.dto.response.SeriesRankingRes;
import com.mangakousei.mangakousei_backend.dto.response.SeriesRiskRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.ReaderVoteBatchService;
import com.mangakousei.mangakousei_backend.service.SeriesRiskService;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminVoteBatchController {

    private final ReaderVoteBatchService voteBatchService;
    private final SeriesRiskService riskService;

    @PostMapping("/vote-batches")
    public ResponseEntity<ApiResponse<ReaderVoteBatchRes>> importVoteBatch(
            @Valid @RequestBody ImportVoteBatchReq req
    ) {
        if (!SecurityUtils.isAdmin()) throw forbidden();
        ReaderVoteBatchRes result = voteBatchService.importBatch(req, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("Vote batch imported", result));
    }

    @GetMapping("/vote-batches")
    public ResponseEntity<ApiResponse<List<ReaderVoteBatchRes>>> getRecentBatches() {
        if (!SecurityUtils.isAdmin()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success("OK", voteBatchService.getRecentBatches()));
    }

    @GetMapping("/vote-batches/{batchId}")
    public ResponseEntity<ApiResponse<ReaderVoteBatchRes>> getBatch(@PathVariable Long batchId) {
        if (!SecurityUtils.isAdmin()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success("OK", voteBatchService.getBatch(batchId)));
    }

    @GetMapping("/rankings/latest")
    public ResponseEntity<ApiResponse<List<SeriesRankingRes>>> getLatestRanking() {
        if (!SecurityUtils.isAdmin()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success("OK", voteBatchService.getLatestRanking()));
    }

    @GetMapping("/series/risks")
    public ResponseEntity<ApiResponse<List<SeriesRiskRes>>> getRisks() {
        if (!SecurityUtils.isAdmin()) throw forbidden();
        return ResponseEntity.ok(ApiResponse.success("OK", riskService.getLatestRisks()));
    }

    private CustomAppException forbidden() {
        return new CustomAppException("Khong co quyen", HttpStatus.FORBIDDEN);
    }
}
