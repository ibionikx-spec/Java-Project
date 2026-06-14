package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.CreateProposalReq;
import com.mangakousei.mangakousei_backend.dto.request.ReviewProposalReq;
import com.mangakousei.mangakousei_backend.dto.response.ProposalListRes;
import com.mangakousei.mangakousei_backend.dto.response.ProposalRes;
import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.GenreRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesProposalRepository;
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

    public List<ProposalListRes> getProposals(Long tantouId, String status, String search) {
        List<Object[]> rows = proposalRepository.findProposalsRaw(tantouId, status, search);
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
            case "approve" -> proposal.setStatus("approved");
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
}