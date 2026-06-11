package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.InboxItemRes;
import com.mangakousei.mangakousei_backend.repository.ManuscriptRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesProposalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class TantouService {

    private final ManuscriptRepository manuscriptRepository;
    private final SeriesProposalRepository proposalRepository;

    public TantouService(ManuscriptRepository manuscriptRepository,
                         SeriesProposalRepository proposalRepository) {
        this.manuscriptRepository = manuscriptRepository;
        this.proposalRepository = proposalRepository;
    }

    public List<InboxItemRes> getInbox() {
        List<InboxItemRes> items = new ArrayList<>();

        items.addAll(manuscriptRepository.findSubmittedManuscripts());

        items.addAll(proposalRepository.findPendingProposals());

        items.sort(Comparator.comparing(InboxItemRes::getSubmittedAt).reversed());

        return items;
    }
}