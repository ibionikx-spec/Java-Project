package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.NotificationRes;
import com.mangakousei.mangakousei_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationRes>>> getMyNotifications() {
        return ResponseEntity.ok(ApiResponse.success(
                "OK", notificationService.getMyNotifications()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countUnread() {
        long count = notificationService.countUnread();
        return ResponseEntity.ok(ApiResponse.success("OK", Map.of("count", count)));
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        notificationService.markAllRead();
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đã đọc", null));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markOneRead(@PathVariable Long id) {
        notificationService.markOneRead(id);
        return ResponseEntity.ok(ApiResponse.success("OK", null));
    }
}