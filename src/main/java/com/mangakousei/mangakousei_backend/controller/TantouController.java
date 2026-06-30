package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.InboxItemRes;
import com.mangakousei.mangakousei_backend.dto.response.TantouDeadlineRes;
import com.mangakousei.mangakousei_backend.dto.response.TantouSeriesRes;
import com.mangakousei.mangakousei_backend.service.TantouDashboardService;
import com.mangakousei.mangakousei_backend.service.TantouSeriesService;
import com.mangakousei.mangakousei_backend.service.TantouService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tantou")
@RequiredArgsConstructor
public class TantouController {

    private final TantouService tantouService;
    private final TantouSeriesService tantouSeriesService;
    private final TantouDashboardService tantouDashboardService;

    @GetMapping("/inbox")
    public ResponseEntity<ApiResponse<List<InboxItemRes>>> getInbox() {
        Long tantouId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fetched Tantou inbox successfully",
                        tantouService.getInbox(tantouId)
                )
        );
    }

    @GetMapping("/series")
    public ResponseEntity<?> getMySeries() {
        Long tantouId = SecurityUtils.getCurrentUserId();
        List<TantouSeriesRes> series = tantouSeriesService.getSeriesByTantou(tantouId);
        return ResponseEntity.ok(ApiResponse.success("Fetched series", series));
    }

    @GetMapping("/dashboard/deadlines")
    public ResponseEntity<ApiResponse<List<TantouDeadlineRes>>> getDeadlineAlerts() {
        Long tantouId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "OK", tantouDashboardService.getDeadlineAlerts(tantouId)));
    }
}