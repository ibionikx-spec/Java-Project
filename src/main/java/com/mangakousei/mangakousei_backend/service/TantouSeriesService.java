
package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.TantouSeriesRes;
import com.mangakousei.mangakousei_backend.entity.entity.Chapter;
import com.mangakousei.mangakousei_backend.entity.entity.Genre;
import com.mangakousei.mangakousei_backend.entity.entity.PublicationSchedule;
import com.mangakousei.mangakousei_backend.entity.entity.Series;
import com.mangakousei.mangakousei_backend.repository.ChapterPageDeadlineRepository;
import com.mangakousei.mangakousei_backend.repository.ChapterRepository;
import com.mangakousei.mangakousei_backend.repository.PublicationScheduleRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TantouSeriesService {

    private final SeriesRepository seriesRepository;
    private final PublicationScheduleRepository scheduleRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterPageDeadlineRepository deadlineRepository;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<TantouSeriesRes> getSeriesByTantou(Long tantouId) {
        return seriesRepository
                .findByEditorUserIdOrderByApprovedAtDesc(tantouId)
                .stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    private TantouSeriesRes toRes(Series s) {
        Optional<PublicationSchedule> schedule =
                scheduleRepository.findBySeriesSeriesId(s.getSeriesId());

        List<Long> chapterIds = chapterRepository
                .findBySeriesSeriesIdOrderByChapterNumberAsc(s.getSeriesId())
                .stream()
                .map(Chapter::getChapterId)
                .toList();

        long totalDeadlines = chapterIds.stream()
                .mapToLong(deadlineRepository::countByChapterChapterId)
                .sum();

        long submittedDeadlines = chapterIds.stream()
                .mapToLong(cid -> deadlineRepository.countByChapterChapterIdAndStatus(cid, "submitted"))
                .sum();

        return TantouSeriesRes.builder()
                .seriesId(s.getSeriesId())
                .title(s.getTitle())
                .coverImageUrl(s.getCoverImageUrl())
                .seriesStatus(s.getSeriesStatus() != null
                        ? s.getSeriesStatus().getSeriesStatusName() : null)
                .mangakaName(s.getCreator() != null
                        ? s.getCreator().getFullName() : null)
                .mangakaAvatarUrl(s.getCreator() != null
                        ? s.getCreator().getAvatarUrl() : null)
                .chapterCount(s.getChapters() != null ? s.getChapters().size() : 0)
                .genres(s.getGenres() != null
                        ? s.getGenres().stream()
                          .map(Genre::getGenreName)
                          .collect(Collectors.toList())
                        : List.of())
                .approvedAt(s.getApprovedAt() != null
                        ? s.getApprovedAt().format(DATE_FMT) : null)
                .scheduleType(schedule.map(PublicationSchedule::getScheduleType).orElse(null))
                .dayValue(schedule.map(PublicationSchedule::getDayValue).orElse(null))
                .totalPageDeadlines(totalDeadlines)
                .submittedPageDeadlines(submittedDeadlines)
                .build();
    }
}