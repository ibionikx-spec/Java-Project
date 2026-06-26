package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ActivityLogRes;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService logService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<ActivityLogRes>>> getMyLogs(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ActivityLogRes> result = logService.getMyLogs(category, page, size);
        return ResponseEntity.ok(ApiResponse.success("Fetched activity logs", result));
    }

    @GetMapping("/me/recent")
    public ResponseEntity<ApiResponse<List<ActivityLogRes>>> getRecentLogs() {
        List<ActivityLogRes> result = logService.getRecentLogs();
        return ResponseEntity.ok(ApiResponse.success("Fetched recent logs", result));
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Page<ActivityLogRes>>> getAllLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        Page<ActivityLogRes> result = logService.getAllLogs(userId, category, from, to, page, size);
        return ResponseEntity.ok(ApiResponse.success("Fetched all logs", result));
    }
}