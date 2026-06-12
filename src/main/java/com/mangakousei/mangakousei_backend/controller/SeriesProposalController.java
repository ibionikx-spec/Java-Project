package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.CreateProposalReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.ProposalListRes;
import com.mangakousei.mangakousei_backend.dto.response.ProposalRes;
import com.mangakousei.mangakousei_backend.service.SeriesProposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/tantou/proposals")
    public ResponseEntity<ApiResponse<List<ProposalListRes>>> getProposals(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        List<ProposalListRes> proposals = seriesProposalService.getProposals(status, search);
        return ResponseEntity.ok(
                ApiResponse.success("Proposals fetched successfully", proposals)
        );
    }

    /*
    *  @GetMapping("/tantou/proposals/{proposalId}")
    public ResponseEntity<ApiResponse<ProposalDetailRes>> getProposalDetail(
            @PathVariable Long proposalId
    ) {
        ProposalDetailRes detail = seriesProposalService.getProposalDetail(proposalId);
        return ResponseEntity.ok(
                ApiResponse.success("Proposal detail fetched successfully", detail)
        );
    }

    // Tantou gửi review (approve / revision / reject)
    @PatchMapping("/tantou/proposals/{proposalId}/review")
    public ResponseEntity<ApiResponse<Void>> reviewProposal(
            @PathVariable Long proposalId,
            @RequestBody @Valid ReviewProposalReq request
    ) {
        seriesProposalService.reviewProposal(proposalId, request);
        return ResponseEntity.ok(
                ApiResponse.success("Review submitted successfully", null)
        );
    }
}

    *
    * */
}