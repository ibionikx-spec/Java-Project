package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.AdminDashboardStatsRes;
import com.mangakousei.mangakousei_backend.dto.response.TantouSeriesRankRes;
import com.mangakousei.mangakousei_backend.entity.entity.Chapter;
import com.mangakousei.mangakousei_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final SeriesRepository seriesRepository;
    private final UserRepository userRepository;
    private final SeriesProposalRepository proposalRepository;
    private final ChapterRepository chapterRepository;
    private final ReaderInteractionRepository interactionRepository;
    private final ReaderVoteRepository readerVoteRepository;

    @Transactional(readOnly = true)
    public AdminDashboardStatsRes getStats() {
        long totalSeries  = seriesRepository.count();
        long totalMangaka = userRepository.countByRoleName("MANGAKA");
        long totalTantou  = userRepository.countByRoleName("TANTOU");
        long totalAssist  = userRepository.countByRoleName("ASSISTANT");

        long pendingProposals = proposalRepository.countByStatus("pending_admin");
        long approvedProposals = proposalRepository.countByStatus("approved");

        long pendingChapters  = chapterRepository
                .countByChapterStatusChapterStatusName("pending_publish");
        long publishedChapters = chapterRepository
                .countByChapterStatusChapterStatusName("published");

        return AdminDashboardStatsRes.builder()
                .totalSeries(totalSeries)
                .totalMangaka(totalMangaka)
                .totalTantou(totalTantou)
                .totalAssistant(totalAssist)
                .pendingAdminProposals(pendingProposals)
                .approvedProposals(approvedProposals)
                .pendingPublishChapters(pendingChapters)
                .publishedChapters(publishedChapters)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TantouSeriesRankRes> getTopSeries() {
        return seriesRepository.findAll().stream()
                .map(s -> {
                    long importedVotes = readerVoteRepository.sumVotesBySeriesId(s.getSeriesId());
                    long readerVotes   = interactionRepository.countBySeriesSeriesIdAndVotedTrue(s.getSeriesId());
                    long totalVotes    = importedVotes + readerVotes;

                    double importedRating = readerVoteRepository.averageScoreBySeriesId(s.getSeriesId());
                    double readerRating   = interactionRepository.averageRatingBySeriesId(s.getSeriesId());
                    long   ratingCount    = interactionRepository.countBySeriesSeriesIdAndRatingIsNotNull(s.getSeriesId());
                    double rating = ratingCount > 0 ? readerRating : importedRating;

                    var latestChapter = chapterRepository
                            .findTopBySeriesSeriesIdOrderByChapterNumberDesc(s.getSeriesId());

                    return TantouSeriesRankRes.builder()
                            .seriesId(s.getSeriesId())
                            .title(s.getTitle())
                            .mangakaName(s.getCreator() != null ? s.getCreator().getFullName() : null)
                            .latestChapter(latestChapter.map(Chapter::getChapterNumber).orElse(null))
                            .latestChapterTitle(latestChapter.map(Chapter::getTitle).orElse(null))
                            .voteCount(totalVotes)
                            .rating(Math.round(rating * 10.0) / 10.0)
                            .chapterCount(s.getChapters() != null ? s.getChapters().size() : 0)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getVoteCount(), a.getVoteCount()))
                .limit(5)
                .collect(Collectors.toList());
    }
}