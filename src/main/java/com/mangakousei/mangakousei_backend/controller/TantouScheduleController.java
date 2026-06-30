package com.mangakousei.mangakousei_backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mangakousei.mangakousei_backend.dto.response.ScheduleEventResponse;
import com.mangakousei.mangakousei_backend.service.TantouScheduleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/tantou")
@RequiredArgsConstructor
public class TantouScheduleController {

    private final TantouScheduleService scheduleService;

    @GetMapping("/schedule")
    public ResponseEntity<List<ScheduleEventResponse>> getSchedule(
            @RequestParam Long tantouId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String status) {
        List<ScheduleEventResponse> events = scheduleService.getScheduleEvents(tantouId, from, to, status);
        return ResponseEntity.ok(events);
    }
}