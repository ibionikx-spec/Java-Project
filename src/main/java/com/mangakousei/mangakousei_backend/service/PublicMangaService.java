package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.ReaderInteractionReq;
import com.mangakousei.mangakousei_backend.dto.response.*;
import com.mangakousei.mangakousei_backend.entity.engagement.ReaderInteraction;
import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PublicMangaService {
    private final SeriesRepository seriesRepository;
    private final ChapterRepository chapterRepository;
    private final PageRepository pageRepository;
    private final ReaderVoteRepository readerVoteRepository;
    private final ReaderInteractionRepository interactionRepository;

    @Transactional(readOnly = true)
    public List<PublicSeriesRes> getSeries() {
        return seriesRepository.findAllPublicSeries().stream().map(series -> toSeries(series, false)).toList();
    }

    @Transactional(readOnly = true)
    public PublicSeriesRes getSeries(Long seriesId) {
        return toSeries(findSeries(seriesId), true);
    }

    @Transactional(readOnly = true)
    public List<PublicChapterRes> getChapters(Long seriesId) {
        findSeries(seriesId);
        return chapterRepository.findBySeriesSeriesIdAndSeriesApprovedAtIsNotNullOrderByChapterNumberDesc(seriesId)
                .stream().map(this::toChapter).toList();
    }

    @Transactional
    public PublicSeriesRes vote(Long seriesId, ReaderInteractionReq request) {
        Series series = findSeries(seriesId);
        ReaderInteraction interaction = getInteraction(series, request.getVisitorId());
        interaction.setVoted(true);
        interactionRepository.save(interaction);
        return toSeries(series, false);
    }

    @Transactional
    public PublicSeriesRes rate(Long seriesId, ReaderInteractionReq request) {
        if (request.getRating() == null) {
            throw new CustomAppException("Rating is required", HttpStatus.BAD_REQUEST);
        }
        Series series = findSeries(seriesId);
        ReaderInteraction interaction = getInteraction(series, request.getVisitorId());
        interaction.setRating(request.getRating());
        interactionRepository.save(interaction);
        return toSeries(series, false);
    }

    @Transactional(readOnly = true)
    public List<PublicSeriesRes> getRanking() {
        return getSeries().stream()
                .sorted(Comparator.comparingLong(PublicSeriesRes::getVoteCount).reversed()
                        .thenComparing(PublicSeriesRes::getRating, Comparator.reverseOrder()))
                .toList();
    }

    private Series findSeries(Long id) {
        return seriesRepository.findPublicSeriesById(id)
                .orElseThrow(() -> new CustomAppException("Series not found", HttpStatus.NOT_FOUND));
    }

    private ReaderInteraction getInteraction(Series series, String visitorId) {
        return interactionRepository.findBySeriesSeriesIdAndVisitorId(series.getSeriesId(), visitorId)
                .orElseGet(() -> ReaderInteraction.builder().series(series).visitorId(visitorId).build());
    }

    private PublicSeriesRes toSeries(Series series, boolean includeChapters) {
        long importedVotes = readerVoteRepository.sumVotesBySeriesId(series.getSeriesId());
        long readerVotes = interactionRepository.countBySeriesSeriesIdAndVotedTrue(series.getSeriesId());
        double importedRating = readerVoteRepository.averageScoreBySeriesId(series.getSeriesId());
        double readerRating = interactionRepository.averageRatingBySeriesId(series.getSeriesId());
        long readerRatingCount = interactionRepository.countBySeriesSeriesIdAndRatingIsNotNull(series.getSeriesId());
        double rating = readerRatingCount > 0 ? readerRating : importedRating;
        List<PublicChapterRes> chapters = includeChapters ? getChapters(series.getSeriesId()) : List.of();
        int chapterCount = chapterRepository.findBySeriesSeriesIdAndSeriesApprovedAtIsNotNullOrderByChapterNumberDesc(series.getSeriesId()).size();

        return PublicSeriesRes.builder()
                .seriesId(series.getSeriesId()).title(series.getTitle()).description(series.getDescription())
                .coverImageUrl(series.getCoverImageUrl())
                .mangakaName(series.getCreator() == null ? null : series.getCreator().getFullName())
                .seriesStatus(series.getSeriesStatus() == null ? null : series.getSeriesStatus().getSeriesStatusName())
                .genres(series.getGenres().stream().map(genre -> genre.getGenreName()).toList())
                .chapterCount(chapterCount).views(0).voteCount(importedVotes + readerVotes)
                .rating(Math.round(rating * 10.0) / 10.0).approvedAt(series.getApprovedAt()).chapters(chapters).build();
    }

    private PublicChapterRes toChapter(Chapter chapter) {
        List<PublicPageRes> pages = pageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapter.getChapterId())
                .stream().filter(page -> page.getFileUrl() != null && !page.getFileUrl().isBlank())
                .map(this::toPage).toList();
        return PublicChapterRes.builder().chapterId(chapter.getChapterId()).chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle()).createdAt(chapter.getCreatedAt())
                .chapterStatus(chapter.getChapterStatus() == null ? null : chapter.getChapterStatus().getChapterStatusName())
                .pages(pages).build();
    }

    private PublicPageRes toPage(Page page) {
        return PublicPageRes.builder().pageId(page.getPageId()).pageNumber(page.getPageNumber()).fileUrl(page.getFileUrl()).build();
    }
}
