package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.TantouReportStatsRes;
import com.mangakousei.mangakousei_backend.dto.response.TantouSeriesRankRes;
import com.mangakousei.mangakousei_backend.entity.entity.Chapter;
import com.mangakousei.mangakousei_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TantouReportService {

    private final SeriesRepository seriesRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterPageDeadlineRepository deadlineRepository;
    private final ReaderInteractionRepository interactionRepository;
    private final ReaderVoteRepository readerVoteRepository;

    @Transactional(readOnly = true)
    public TantouReportStatsRes getStats(Long tantouId) {
        long totalSeries = seriesRepository.countByEditorUserId(tantouId);

        List<Long> chapterIds = chapterRepository
                .findBySeriesEditorUserId(tantouId)
                .stream()
                .map(Chapter::getChapterId)
                .toList();

        long publishedChapters = chapterRepository
                .countBySeriesEditorAndStatus(tantouId, "published");

        long pendingReviewChapters = chapterRepository
                .countBySeriesEditorAndStatus(tantouId, "pending_publish");

        long totalDeadlines = chapterIds.stream()
                .mapToLong(deadlineRepository::countByChapterChapterId)
                .sum();

        LocalDate today = LocalDate.now();
        long overdueDeadlines = chapterIds.stream()
                .mapToLong(cid -> deadlineRepository
                        .countOverdueByChapterChapterId(cid, today))
                .sum();

        long submittedDeadlines = chapterIds.stream()
                .mapToLong(cid -> deadlineRepository
                        .countByChapterChapterIdAndStatus(cid, "submitted"))
                .sum();

        return TantouReportStatsRes.builder()
                .totalSeries(totalSeries)
                .publishedChapters(publishedChapters)
                .pendingReviewChapters(pendingReviewChapters)
                .totalDeadlines(totalDeadlines)
                .overdueDeadlines(overdueDeadlines)
                .submittedDeadlines(submittedDeadlines)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TantouSeriesRankRes> getRanking(Long tantouId) {
        return seriesRepository
                .findByEditorUserIdOrderByApprovedAtDesc(tantouId)
                .stream()
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
                .collect(Collectors.toList());
    }
}