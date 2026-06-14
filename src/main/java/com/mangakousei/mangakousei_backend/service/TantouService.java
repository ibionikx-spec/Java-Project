package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.InboxItemRes;
import com.mangakousei.mangakousei_backend.repository.ManuscriptRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TantouService {

    private final ManuscriptRepository manuscriptRepository;
    private final SeriesProposalRepository proposalRepository;

    public List<InboxItemRes> getInbox(Long tantouId) {
        List<InboxItemRes> items = new ArrayList<>();

        items.addAll(manuscriptRepository.findSubmittedManuscripts());

        items.addAll(proposalRepository.findPendingProposalsByTantouId(tantouId));

        items.sort(Comparator.comparing(InboxItemRes::getSubmittedAt).reversed());

        return items;
    }
}