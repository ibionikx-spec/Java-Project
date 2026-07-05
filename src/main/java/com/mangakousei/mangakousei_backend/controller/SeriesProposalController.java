package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.CreateProposalReq;
import com.mangakousei.mangakousei_backend.dto.request.UpdateProposalReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.ProposalListRes;
import com.mangakousei.mangakousei_backend.dto.response.ProposalRes;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.service.SeriesProposalService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SeriesProposalController {

    private final SeriesProposalService seriesProposalService;

    @PostMapping("/proposals")
    public ResponseEntity<ApiResponse<ProposalRes>> createProposal(
            @RequestBody @Valid CreateProposalReq request
    ) {
        ProposalRes proposalRes = seriesProposalService.createProposal(request);
        return ResponseEntity.ok(ApiResponse.success("Proposal created successfully", proposalRes));
    }

    @GetMapping("/proposals/{id}")
    public ResponseEntity<ApiResponse<ProposalListRes>> getMyProposalDetail(
            @PathVariable Long id
    ) {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        ProposalListRes result = seriesProposalService.getMyProposalDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Proposal fetched successfully", result));
    }

    @PutMapping("/proposals/{id}")
    public ResponseEntity<ApiResponse<ProposalListRes>> updateProposal(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProposalReq request
    ) {
        if (!SecurityUtils.isMangaka()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        ProposalListRes result = seriesProposalService.updateProposal(id, request);
        return ResponseEntity.ok(ApiResponse.success("Proposal updated successfully", result));
    }

    @GetMapping("/tantou/proposals")
    public ResponseEntity<ApiResponse<List<ProposalListRes>>> getProposals(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        Long tantouId = SecurityUtils.getCurrentUserId();
        List<ProposalListRes> proposals = seriesProposalService.getProposals(tantouId, status, search);
        return ResponseEntity.ok(
                ApiResponse.success("Proposals fetched successfully", proposals)
        );
    }
}