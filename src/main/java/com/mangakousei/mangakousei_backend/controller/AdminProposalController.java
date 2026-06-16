package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.ProposalListRes;
import com.mangakousei.mangakousei_backend.dto.request.ReviewProposalReq;
import com.mangakousei.mangakousei_backend.service.SeriesProposalService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/proposals")
@RequiredArgsConstructor
public class AdminProposalController {

    private final SeriesProposalService proposalService;

    @PatchMapping("/{id}/review")
    public ResponseEntity<?> adminReview(@PathVariable Long id,
                                         @Valid @RequestBody ReviewProposalReq request) {
        if (!SecurityUtils.isAdmin()) {
            throw new CustomAppException(
                    "Bạn không có quyền phê duyệt proposal", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(ApiResponse.success(
            "Admin reviewed proposal successfully",
            proposalService.adminReviewProposal(id, request)
        ));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingAdminProposals() {
        if (!SecurityUtils.isAdmin()) {
            throw new CustomAppException("Không có quyền truy cập", HttpStatus.FORBIDDEN);
        }
        List<ProposalListRes> proposals = proposalService.getAdminPendingProposals();
        return ResponseEntity.ok(ApiResponse.success("Fetched pending proposals for admin", proposals));
    }

    @PatchMapping("/{id}/cancel-approve")
    public ResponseEntity<?> cancelApprove(@PathVariable Long id) {
        if (!SecurityUtils.isAdmin()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        proposalService.cancelApprove(id);
        return ResponseEntity.ok(ApiResponse.success("Đã huỷ duyệt, proposal về pending_admin", null));
    }
}
