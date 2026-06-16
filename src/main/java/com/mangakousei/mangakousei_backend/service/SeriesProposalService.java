package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.CreateProposalReq;
import com.mangakousei.mangakousei_backend.dto.request.ReviewProposalReq;
import com.mangakousei.mangakousei_backend.dto.response.ProposalListRes;
import com.mangakousei.mangakousei_backend.dto.response.ProposalRes;
import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.entity.status.SeriesStatus;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class SeriesProposalService {

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
        return new ProposalRes(saved.getProposalId(), saved.getStatus());
    }

    public List<ProposalListRes> getAdminPendingProposals() {
        List<Object[]> rows = proposalRepository.findPendingAdminProposals();
        return mapRowsToProposalList(rows);
    }

    public List<ProposalListRes> getProposals(Long tantouId, String status, String search) {
        List<Object[]> rows = proposalRepository.findProposalsRaw(tantouId, status, search);
        return mapRowsToProposalList(rows);
    }

    @Transactional
    public void reviewProposal(Long proposalId, ReviewProposalReq request) {
        SeriesProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy proposal", HttpStatus.NOT_FOUND));

        if (!List.of("pending", "revision").contains(proposal.getStatus())) {
            throw new CustomAppException(
                    "Chỉ có thể duyệt proposal ở trạng thái chờ hoặc cần sửa",
                    HttpStatus.BAD_REQUEST);
        }

        User currentUser = getCurrentUser();

        switch (request.getDecision()) {
            case "approve" -> {
            proposal.setStatus("pending_admin");
            }
            case "revision" -> {
                if (request.getFeedback() == null || request.getFeedback().isBlank())
                    throw new CustomAppException(
                            "Phản hồi yêu cầu sửa không được để trống", HttpStatus.BAD_REQUEST);
                proposal.setStatus("revision");
                proposal.setRevisionFeedback(request.getFeedback());
            }
            case "reject" -> {
                if (request.getReason() == null || request.getReason().isBlank())
                    throw new CustomAppException(
                            "Lý do từ chối không được để trống", HttpStatus.BAD_REQUEST);
                proposal.setStatus("rejected");
                proposal.setRejectionReason(request.getReason());
            }
            default -> throw new CustomAppException(
                    "Decision không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        proposal.setReviewedBy(currentUser);
        proposal.setDecidedAt(LocalDateTime.now());
        proposal.setUpdatedAt(LocalDateTime.now());
        proposalRepository.save(proposal);
    }

    @Transactional
public Map<String, Object> adminReviewProposal(Long proposalId, ReviewProposalReq request) {
        SeriesProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy proposal", HttpStatus.NOT_FOUND));
 
        if (!"pending_admin".equals(proposal.getStatus())) {
            throw new CustomAppException(
                    "Proposal này không ở trạng thái chờ admin phê duyệt",
                    HttpStatus.BAD_REQUEST);
        }
 
        switch (request.getDecision()) {
            case "approve" -> {
                proposal.setStatus("approved_pending_schedule");
                proposal.setUpdatedAt(LocalDateTime.now());
                proposalRepository.save(proposal);
                return Map.of(
                    "proposalId", proposal.getProposalId(),
                    "status", "approved_pending_schedule"
                );
            }
            case "reject" -> {
                if (request.getReason() == null || request.getReason().isBlank())
                    throw new CustomAppException(
                            "Lý do từ chối không được để trống", HttpStatus.BAD_REQUEST);
                proposal.setStatus("rejected");
                proposal.setRejectionReason(request.getReason());
                proposal.setUpdatedAt(LocalDateTime.now());
                proposalRepository.save(proposal);
                return Map.of(
                    "proposalId", proposal.getProposalId(),
                    "status", "rejected"
                );
            }
            default -> throw new CustomAppException(
                    "Decision không hợp lệ: chỉ chấp nhận 'approve' hoặc 'reject'",
                    HttpStatus.BAD_REQUEST);
        }
    } 

    @Transactional
    public void reopenProposal(Long proposalId) {
        SeriesProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy proposal", HttpStatus.NOT_FOUND));

        proposal.setStatus("pending");
        proposal.setRejectionReason(null);
        proposal.setRevisionFeedback(null);
        proposal.setReviewedBy(null);
        proposal.setDecidedAt(null);
        proposal.setUpdatedAt(LocalDateTime.now());
        proposalRepository.save(proposal);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new CustomAppException("User not logged in", HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new CustomAppException(
                        "User not found", HttpStatus.NOT_FOUND));
    }

    private List<ProposalListRes.GenreInfo> parseGenreList(String json) {
        try {
            return new ObjectMapper().readValue(
                    json, new TypeReference<List<ProposalListRes.GenreInfo>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse genres JSON: " + json, e);
        }
    }

    private List<ProposalListRes.CharacterInfo> parseCharacterList(String json) {
        try {
            return new ObjectMapper().readValue(
                    json, new TypeReference<List<ProposalListRes.CharacterInfo>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse characters JSON: " + json, e);
        }
    }

    private List<ProposalListRes> mapRowsToProposalList(List<Object[]> rows) {
        List<ProposalListRes> result = new ArrayList<>();
        for (Object[] row : rows) {
            ProposalListRes dto = new ProposalListRes();
            dto.setProposalId(((Number) row[0]).longValue());
            dto.setWorkingTitle((String) row[1]);
            dto.setSynopsis((String) row[2]);
            dto.setTargetAudience((String) row[3]);
            dto.setStatus((String) row[4]);
            dto.setCreatedAt((LocalDateTime) row[5]);
            dto.setNameSummary((String) row[6]);
            dto.setRejectionReason((String) row[7]);
            dto.setRevisionFeedback((String) row[8]);
            dto.setSketchImageUrl((String) row[9]);

            ProposalListRes.MangakaInfo mangaka = new ProposalListRes.MangakaInfo();
            mangaka.setUserId(((Number) row[10]).longValue());
            mangaka.setFullName((String) row[11]);
            mangaka.setAvatarUrl((String) row[12]);
            dto.setMangaka(mangaka);

            dto.setGenres(parseGenreList((String) row[13]));
            dto.setCharacters(parseCharacterList((String) row[14]));

            result.add(dto);
        }
        return result;
    }

    @Transactional
    public void cancelApprove(Long proposalId) {
        SeriesProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy proposal", HttpStatus.NOT_FOUND));
 
        if (!"approved_pending_schedule".equals(proposal.getStatus())) {
            throw new CustomAppException(
                    "Proposal không ở trạng thái chờ set lịch",
                    HttpStatus.BAD_REQUEST);
        }
 
        proposal.setStatus("pending_admin");
        proposal.setUpdatedAt(LocalDateTime.now());
        proposalRepository.save(proposal);
    }

    @Transactional
    public Map<String, Object> confirmScheduleAndCreateSeries(Long proposalId,
                                                               String scheduleType,
                                                               Integer dayValue) {
        SeriesProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy proposal", HttpStatus.NOT_FOUND));
 
        if (!"approved_pending_schedule".equals(proposal.getStatus())) {
            throw new CustomAppException(
                    "Proposal không ở trạng thái chờ set lịch",
                    HttpStatus.BAD_REQUEST);
        }
 
        User admin = getCurrentUser();
 
        Series savedSeries = createSeriesFromProposal(proposal, admin);
 
        PublicationSchedule schedule = PublicationSchedule.builder()
                .series(savedSeries)
                .scheduleType(scheduleType)
                .dayValue(dayValue)
                .build();
        publicationScheduleRepository.save(schedule);
 
        proposal.setStatus("approved");
        proposal.setDecidedAt(LocalDateTime.now());
        proposal.setUpdatedAt(LocalDateTime.now());
        proposalRepository.save(proposal);
 
        return Map.of(
            "seriesId", savedSeries.getSeriesId(),
            "proposalId", proposal.getProposalId(),
            "status", "approved"
        );
    }

    private Series createSeriesFromProposal(SeriesProposal proposal, User admin) {
 
        SeriesStatus approvedStatus = seriesStatusRepository
                .findBySeriesStatusName("approved")
                .or(() -> seriesStatusRepository.findBySeriesStatusName("active"))
                .or(() -> seriesStatusRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy SeriesStatus trong database",
                        HttpStatus.INTERNAL_SERVER_ERROR));
 
        PublicationType publicationType = publicationTypeRepository
                .findById(1L)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy PublicationType trong database",
                        HttpStatus.INTERNAL_SERVER_ERROR));
 
        DecisionType approvedDecisionType = decisionTypeRepository
                .findByDecisionTypeName("approved")
                .or(() -> decisionTypeRepository.findByDecisionTypeName("approve"))
                .or(() -> decisionTypeRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy DecisionType 'approved' trong database",
                        HttpStatus.INTERNAL_SERVER_ERROR));
 
        List<Genre> genres = proposal.getProposalGenres().stream()
                .map(ProposalGenre::getGenre)
                .toList();
 
        Series series = Series.builder()
                .title(proposal.getWorkingTitle())
                .description(proposal.getSynopsis())
                .creator(proposal.getMangaka())
                .editor(proposal.getAssignedTantou())
                .seriesStatus(approvedStatus)
                .publicationType(publicationType)
                .approvedAt(LocalDateTime.now())
                .genres(new ArrayList<>(genres))
                .build();
 
        Series savedSeries = seriesRepository.save(series);
 
        PublicationDecision decision = new PublicationDecision();
        decision.setSeries(savedSeries);
        decision.setDecisionType(approvedDecisionType);
        decision.setDecider(admin);
        decision.setReason("Admin phê duyệt proposal #"
                + proposal.getProposalId()
                + " - \"" + proposal.getWorkingTitle() + "\"");
        publicationDecisionRepository.save(decision);
 
        return savedSeries;
    }
}