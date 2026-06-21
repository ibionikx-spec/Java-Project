package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.MangakaSeriesRes;
import com.mangakousei.mangakousei_backend.entity.entity.Genre;
import com.mangakousei.mangakousei_backend.entity.entity.PublicationSchedule;
import com.mangakousei.mangakousei_backend.entity.entity.Series;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.PublicationScheduleRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MangakaSeriesService {

    private final SeriesRepository seriesRepository;
    private final PublicationScheduleRepository scheduleRepository;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<MangakaSeriesRes> getSeriesByMangaka(Long mangakaId) {
        List<Series> seriesList =
                seriesRepository.findByCreatorUserIdOrderByApprovedAtDesc(mangakaId);

        return seriesList.stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    private MangakaSeriesRes toRes(Series s) {
        Optional<PublicationSchedule> schedule =
                scheduleRepository.findBySeriesSeriesId(s.getSeriesId());

        return MangakaSeriesRes.builder()
                .seriesId(s.getSeriesId())
                .title(s.getTitle())
                .description(s.getDescription())
                .coverImageUrl(s.getCoverImageUrl())
                .seriesStatus(s.getSeriesStatus() != null
                        ? s.getSeriesStatus().getSeriesStatusName() : null)
                .tantouName(s.getEditor() != null
                        ? s.getEditor().getFullName() : null)
                .tantouAvatarUrl(s.getEditor() != null
                        ? s.getEditor().getAvatarUrl() : null)
                .chapterCount(s.getChapters() != null
                        ? s.getChapters().size() : 0)
                .genres(s.getGenres() != null
                        ? s.getGenres().stream()
                          .map(Genre::getGenreName)
                          .collect(Collectors.toList())
                        : List.of())
                .approvedAt(s.getApprovedAt() != null
                        ? s.getApprovedAt().format(DATE_FMT) : null)
                .scheduleType(schedule.map(PublicationSchedule::getScheduleType).orElse(null))
                .dayValue(schedule.map(PublicationSchedule::getDayValue).orElse(null))
                .build();
    }

    public MangakaSeriesRes getSeriesDetail(Long seriesId, Long mangakaId) {
        Series series = seriesRepository
                .findBySeriesIdAndCreatorUserId(seriesId, mangakaId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy series hoặc bạn không có quyền truy cập",
                        HttpStatus.NOT_FOUND));
        return toRes(series);
    }
}