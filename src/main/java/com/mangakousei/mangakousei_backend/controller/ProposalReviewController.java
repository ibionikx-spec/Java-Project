package com.mangakousei.mangakousei_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mangakousei.mangakousei_backend.dto.request.ReviewProposalReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.service.SeriesProposalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tantou/proposals")
@RequiredArgsConstructor
public class ProposalReviewController {
    private final SeriesProposalService proposalService;

    @PatchMapping("/{id}/review")
    public ResponseEntity<?> reviewProposal(@PathVariable Long id,
                                           @Valid @RequestBody ReviewProposalReq request) {
        proposalService.reviewProposal(id, request);

        return ResponseEntity.ok(ApiResponse.success("Proposal reviewed successfully", null));
    }

    @PatchMapping("/{id}/reopen")
    public ResponseEntity<?> reopenProposal(@PathVariable Long id) {
        proposalService.reopenProposal(id);

        return ResponseEntity.ok(ApiResponse.success("Proposal reopened successfully", null));
    }
}
