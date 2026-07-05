package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.constant.RealtimeQueues;
import com.mangakousei.mangakousei_backend.dto.request.CreateProposalReq;
import com.mangakousei.mangakousei_backend.dto.request.LogContext;
import com.mangakousei.mangakousei_backend.dto.request.ReviewProposalReq;
import com.mangakousei.mangakousei_backend.dto.request.UpdateProposalReq;
import com.mangakousei.mangakousei_backend.dto.response.ProposalListRes;
import com.mangakousei.mangakousei_backend.dto.response.ProposalRes;
import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.entity.status.SeriesStatus;
import com.mangakousei.mangakousei_backend.entity.type.ActionType;
import com.mangakousei.mangakousei_backend.entity.type.DecisionType;
import com.mangakousei.mangakousei_backend.entity.type.PublicationType;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.DecisionTypeRepository;
import com.mangakousei.mangakousei_backend.repository.GenreRepository;
import com.mangakousei.mangakousei_backend.repository.PublicationDecisionRepository;
import com.mangakousei.mangakousei_backend.repository.PublicationScheduleRepository;
import com.mangakousei.mangakousei_backend.repository.PublicationTypeRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesProposalRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesStatusRepository;
import com.mangakousei.mangakousei_backend.repository.TantouMangakaAssignmentRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SeriesProposalService {
    private static final String PROPOSAL_REVISION_LINK_PREFIX = "__PROPOSAL_REVISION_LINK__::";

    private final SeriesProposalRepository proposalRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final TantouMangakaAssignmentRepository assignmentRepository;
    private final SeriesRepository seriesRepository;
    private final SeriesStatusRepository seriesStatusRepository;
    private final PublicationTypeRepository publicationTypeRepository;
    private final DecisionTypeRepository decisionTypeRepository;
    private final PublicationDecisionRepository publicationDecisionRepository;
    private final PublicationScheduleRepository publicationScheduleRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;
    private final ChatService chatService;
    private final RealtimePushService realtimePushService;

    public ProposalRes createProposal(CreateProposalReq request) {
        User mangaka = getCurrentUser();

        User tantou = userRepository.findById(request.getTantouId())
                .orElseThrow(() -> new CustomAppException(
                        "Tantou không tồn tại", HttpStatus.BAD_REQUEST));

        assignmentRepository
                .findByTantou_UserIdAndMangaka_UserIdAndIsActiveTrue(
                        tantou.getUserId(), mangaka.getUserId())
                .orElseThrow(() -> new CustomAppException(
                        "Bạn không được phân công cho Tantou này", HttpStatus.FORBIDDEN));

        SeriesProposal proposal = SeriesProposal.builder()
                .mangaka(mangaka)
                .workingTitle(request.getWorkingTitle())
                .synopsis(request.getSynopsis())
                .targetAudience(request.getTargetAudience())
                .nameSummary(request.getNameSummary())
                .sketchImageUrl(request.getSketchImageUrl())
                .assignedTantou(tantou)
                .status("pending")
                .build();

        for (Long genreId : request.getGenreIds()) {
            Genre genre = genreRepository.findById(genreId)
                    .orElseThrow(() -> new RuntimeException("Genre not found: " + genreId));
            proposal.addGenre(genre);
        }

        for (CreateProposalReq.CharacterDto dto : request.getCharacters()) {
            ProposalCharacter character = ProposalCharacter.builder()
                    .characterName(dto.getCharacterName())
                    .role(dto.getRole())
                    .description(dto.getDescription())
                    .build();
            proposal.addCharacter(character);
        }

        SeriesProposal saved = proposalRepository.save(proposal);

        activityLogService.log(LogContext.builder()
                .actionType(ActionType.CREATE_PROPOSAL)
                .detail("Tạo proposal \"" + saved.getWorkingTitle() + "\"")
                .entityType("PROPOSAL")
                .entityId(saved.getProposalId())
                .build());

        notificationService.send(tantou.getUserId(), "PROPOSAL",
                "📋 Proposal mới cần xem xét",
                mangaka.getFullName() + " vừa gửi proposal \""
                        + saved.getWorkingTitle() + "\" – hãy xem xét và phản hồi.");

        pushProposalUpdate(saved);

        return new ProposalRes(saved.getProposalId(), saved.getStatus());
    }

    public ProposalListRes getMyProposalDetail(Long proposalId) {
        User mangaka = getCurrentUser();

        SeriesProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy proposal", HttpStatus.NOT_FOUND));

        if (!proposal.getMangaka().getUserId().equals(mangaka.getUserId())) {
                throw new CustomAppException(
                        "Bạn không có quyền xem proposal này", HttpStatus.FORBIDDEN);
        }

        return toProposalListRes(proposal);
        }

    @Transactional
        public ProposalListRes updateProposal(Long proposalId, UpdateProposalReq request) {
        User mangaka = getCurrentUser();

        SeriesProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy proposal", HttpStatus.NOT_FOUND));

        if (!proposal.getMangaka().getUserId().equals(mangaka.getUserId())) {
                throw new CustomAppException(
                        "Bạn không có quyền sửa proposal này", HttpStatus.FORBIDDEN);
        }

        if (!"revision".equals(proposal.getStatus())) {
                throw new CustomAppException(
                        "Chỉ có thể sửa proposal đang ở trạng thái 'revision'", HttpStatus.BAD_REQUEST);
        }

        proposal.setWorkingTitle(request.getWorkingTitle());
        proposal.setSynopsis(request.getSynopsis());
        proposal.setTargetAudience(request.getTargetAudience());
        proposal.setNameSummary(request.getNameSummary());
        
        if (request.getSketchImageUrl() != null && !request.getSketchImageUrl().isBlank()) {
                proposal.setSketchImageUrl(request.getSketchImageUrl());
        }

        proposal.getProposalGenres().clear();
        for (Long genreId : request.getGenreIds()) {
                Genre genre = genreRepository.findById(genreId)
                        .orElseThrow(() -> new CustomAppException(
                                "Genre not found: " + genreId, HttpStatus.BAD_REQUEST));
                proposal.addGenre(genre);
        }

        proposal.getProposalCharacters().clear();
        
        for (CreateProposalReq.CharacterDto dto : request.getCharacters()) {
                ProposalCharacter character = ProposalCharacter.builder()
                        .characterName(dto.getCharacterName())
                        .role(dto.getRole())
                        .description(dto.getDescription())
                        .build();
                proposal.addCharacter(character);
        }

        proposal.setStatus("pending");
        proposal.setRevisionFeedback(null);
        proposal.setReviewedBy(null);
        proposal.setDecidedAt(null);
        proposal.setUpdatedAt(LocalDateTime.now());

        SeriesProposal saved = proposalRepository.save(proposal);
        
        activityLogService.log(LogContext.builder()
                .actionType(ActionType.CREATE_PROPOSAL)
                .detail("Mangaka nộp lại proposal \"" + saved.getWorkingTitle() + "\" sau khi chỉnh sửa")
                .entityType("PROPOSAL")
                .entityId(saved.getProposalId())
                .build());

        if (saved.getAssignedTantou() != null) {
                notificationService.send(saved.getAssignedTantou().getUserId(), "PROPOSAL",
                        "🔄 Proposal đã được nộp lại",
                        mangaka.getFullName() + " đã chỉnh sửa và nộp lại proposal \""
                                + saved.getWorkingTitle() + "\" -- hãy xem xét lại.");
        }

        pushProposalUpdate(saved);
        return toProposalListRes(saved);
    }

    
}
