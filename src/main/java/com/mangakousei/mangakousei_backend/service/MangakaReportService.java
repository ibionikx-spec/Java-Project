package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.DailyProductionRes;
import com.mangakousei.mangakousei_backend.dto.response.MangakaReportStatsRes;
import com.mangakousei.mangakousei_backend.entity.entity.ChapterPageDeadline;
import com.mangakousei.mangakousei_backend.repository.ChapterPageDeadlineRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MangakaReportService {

    private final SeriesRepository seriesRepository;
    private final ChapterPageDeadlineRepository deadlineRepository;

    private static final List<String> DONE_STATUSES = List.of("submitted", "approved");
    private static final Map<Integer, String> WEEKDAY_LABEL = Map.of(
            1, "T2", 2, "T3", 3, "T4", 4, "T5", 5, "T6", 6, "T7", 7, "CN"
    );

    @Transactional(readOnly = true)
    public MangakaReportStatsRes getStats(Long mangakaId) {
        long totalSeries = seriesRepository.countByCreatorUserId(mangakaId);
        long totalPages = seriesRepository.countPagesByCreatorUserId(mangakaId);

        long totalDeadlines = deadlineRepository.countByMangakaId(mangakaId);
        long submittedDeadlines = deadlineRepository
                .countByMangakaIdAndStatusIn(mangakaId, DONE_STATUSES);
        long pendingDeadlines = totalDeadlines - submittedDeadlines;

        double completionRate = totalDeadlines > 0
                ? Math.round((submittedDeadlines * 1000.0 / totalDeadlines)) / 10.0
                : 0;

        return MangakaReportStatsRes.builder()
                .totalSeries(totalSeries)
                .totalPages(totalPages)
                .totalDeadlines(totalDeadlines)
                .submittedDeadlines(submittedDeadlines)
                .pendingDeadlines(pendingDeadlines)
                .completionRate(completionRate)
                .build();
    }

    @Transactional(readOnly = true)
    public List<DailyProductionRes> getDailyProduction(Long mangakaId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        List<ChapterPageDeadline> submitted = deadlineRepository
                .findSubmittedByMangakaIdSince(mangakaId, start.atStartOfDay());

        Map<LocalDate, Long> countByDate = submitted.stream()
                .map(d -> d.getSubmittedAt().toLocalDate())
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        List<DailyProductionRes> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = start.plusDays(i);
            DayOfWeek dow = day.getDayOfWeek();
            result.add(DailyProductionRes.builder()
                    .date(day)
                    .dayLabel(WEEKDAY_LABEL.get(dow.getValue()))
                    .submittedCount(countByDate.getOrDefault(day, 0L))
                    .build());
        }
        return result;
    }
}