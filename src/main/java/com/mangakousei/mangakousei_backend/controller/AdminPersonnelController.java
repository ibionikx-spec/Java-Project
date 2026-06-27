package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.AdminCreateUserReq;
import com.mangakousei.mangakousei_backend.dto.response.*;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.AdminPersonnelService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/personnel")
@RequiredArgsConstructor
public class AdminPersonnelController {

    private final AdminPersonnelService personnelService;

    private void requireAdmin() {
        if (!SecurityUtils.isAdmin())
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<PersonnelUserRes>>> getUsersByRole(
            @RequestParam String role) {
        requireAdmin();
        return ResponseEntity.ok(ApiResponse.success("OK",
                personnelService.getUsersByRole(role)));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<PersonnelUserRes>> createUser(
            @Valid @RequestBody AdminCreateUserReq req) {
        requireAdmin();
        return ResponseEntity.ok(ApiResponse.success(
                "Tạo tài khoản thành công", personnelService.createUser(req)));
    }

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<List<TantouMangakaAssignRes>>> getAssignments() {
        requireAdmin();
        return ResponseEntity.ok(ApiResponse.success("OK",
                personnelService.getAllAssignments()));
    }

    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<TantouMangakaAssignRes>> assign(
            @RequestBody Map<String, Long> body) {
        requireAdmin();
        Long tantouId  = body.get("tantouId");
        Long mangakaId = body.get("mangakaId");
        if (tantouId == null || mangakaId == null)
            throw new CustomAppException("tantouId và mangakaId là bắt buộc", HttpStatus.BAD_REQUEST);
        return ResponseEntity.ok(ApiResponse.success(
                "Phân công thành công", personnelService.assignTantouToMangaka(tantouId, mangakaId)));
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<ApiResponse<Void>> removeAssignment(@PathVariable Long id) {
        requireAdmin();
        personnelService.removeAssignment(id);
        return ResponseEntity.ok(ApiResponse.success("Đã hủy phân công", null));
    }
}