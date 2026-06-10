package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.CreateProposalReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.ProposalRes;
import com.mangakousei.mangakousei_backend.service.SeriesProposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}