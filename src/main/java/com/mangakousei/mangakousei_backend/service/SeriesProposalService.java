package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.CreateProposalReq;
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