package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.CreateProposalReq;
import com.mangakousei.mangakousei_backend.dto.response.ProposalListRes;
import com.mangakousei.mangakousei_backend.dto.response.ProposalRes;
import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.repository.GenreRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesProposalRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    public ProposalRes createProposal(CreateProposalReq request) {
        User mangaka = getCurrentUser();

        SeriesProposal proposal = SeriesProposal.builder()
                .mangaka(mangaka)
                .workingTitle(request.getWorkingTitle())
                .synopsis(request.getSynopsis())
                .targetAudience(request.getTargetAudience())
                .nameSummary(request.getNameSummary())
                .sketchImageUrl(request.getSketchImageUrl())
                .status("pending")
                .build();

        for (Long genreId : request.getGenreIds()) {
            Genre genre = genreRepository.findById(genreId)
                    .orElseThrow(() -> new RuntimeException("Genre not found with id: " + genreId));
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

    public List<ProposalListRes> getProposals(String status, String search) {
        List<Object[]> rows = proposalRepository.findProposalsRaw(status, search);
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

            String genresJson = (String) row[13];
            dto.setGenres(parseGenreList(genresJson));

            String charactersJson = (String) row[14];
            dto.setCharacters(parseCharacterList(charactersJson));

            result.add(dto);
        }
        return result;
    }

    private List<ProposalListRes.GenreInfo> parseGenreList(String json) throws RuntimeException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<List<ProposalListRes.GenreInfo>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse genres JSON: " + json, e);
        }
    }

    private List<ProposalListRes.CharacterInfo> parseCharacterList(String json) throws RuntimeException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<List<ProposalListRes.CharacterInfo>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse characters JSON: " + json, e);
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new RuntimeException("User not logged in");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}