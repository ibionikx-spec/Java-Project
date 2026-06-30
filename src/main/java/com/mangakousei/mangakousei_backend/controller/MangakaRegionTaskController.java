package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.CreateRegionReq;
import com.mangakousei.mangakousei_backend.dto.request.CreateTaskReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.RegionRes;
import com.mangakousei.mangakousei_backend.dto.response.TaskRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.RegionTaskService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mangaka")
@RequiredArgsConstructor
public class MangakaRegionTaskController {

    private final RegionTaskService regionTaskService;

    @GetMapping("/pages/{pageId}/regions")
    public ResponseEntity<?> getRegions(@PathVariable Long pageId) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        List<RegionRes> regions = regionTaskService.getRegionsByPage(pageId);
        return ResponseEntity.ok(ApiResponse.success("Fetched regions", regions));
    }

    @PostMapping("/regions")
    public ResponseEntity<?> createRegion(@Valid @RequestBody CreateRegionReq req) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        RegionRes result = regionTaskService.createRegion(req);
        return ResponseEntity.ok(ApiResponse.success("Region created", result));
    }

    @PutMapping("/regions/{regionId}")
    public ResponseEntity<?> updateRegion(@PathVariable Long regionId,
                                          @Valid @RequestBody CreateRegionReq req) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        RegionRes result = regionTaskService.updateRegion(regionId, req);
        return ResponseEntity.ok(ApiResponse.success("Region updated", result));
    }

    @DeleteMapping("/regions/{regionId}")
    public ResponseEntity<?> deleteRegion(@PathVariable Long regionId) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        regionTaskService.deleteRegion(regionId);
        return ResponseEntity.ok(ApiResponse.success("Region deleted", null));
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@Valid @RequestBody CreateTaskReq req) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        TaskRes result = regionTaskService.createTask(req);
        return ResponseEntity.ok(ApiResponse.success("Task created", result));
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable Long taskId,
                                        @Valid @RequestBody CreateTaskReq req) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        TaskRes result = regionTaskService.updateTask(taskId, req);
        return ResponseEntity.ok(ApiResponse.success("Task updated", result));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId) {
        if (!SecurityUtils.isMangaka()) throw forbidden();
        regionTaskService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("Task deleted", null));
    }

    private CustomAppException forbidden() {
        return new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
    }
}