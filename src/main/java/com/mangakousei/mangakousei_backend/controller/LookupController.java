package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.repository.RegionTypeRepository;
import com.mangakousei.mangakousei_backend.repository.TaskTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LookupController {

    private final RegionTypeRepository regionTypeRepository;
    private final TaskTypeRepository taskTypeRepository;

    @GetMapping("/region-types")
    public ResponseEntity<?> getRegionTypes() {
        List<Map<String, Object>> types = regionTypeRepository.findAll().stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getRegionTypeId(),
                        "name", t.getRegionTypeName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Region types", types));
    }

    @GetMapping("/task-types")
    public ResponseEntity<?> getTaskTypes() {
        List<Map<String, Object>> types = taskTypeRepository.findAll().stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getTaskTypeId(),
                        "name", t.getTaskTypeName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Task types", types));
    }
}