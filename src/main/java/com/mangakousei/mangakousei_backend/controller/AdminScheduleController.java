
package com.mangakousei.mangakousei_backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mangakousei.mangakousei_backend.dto.request.ConfirmScheduleReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.PublicationScheduleService;
import com.mangakousei.mangakousei_backend.service.SeriesProposalService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/schedules")
@RequiredArgsConstructor
public class AdminScheduleController {

    private final SeriesProposalService proposalService;
    private final PublicationScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<?> getAllSchedules() {
        if (!SecurityUtils.isAdmin()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(ApiResponse.success("OK", scheduleService.getAllSchedules()));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmSchedule(@Valid @RequestBody ConfirmScheduleReq req) {
        if (!SecurityUtils.isAdmin()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        Map<String, Object> seriesId = proposalService.confirmScheduleAndCreateSeries(
                req.getProposalId(),
                req.getScheduleType(),
                req.getDayValue()
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Series đã được tạo thành công", Map.of("seriesId", seriesId)));
    }
}
