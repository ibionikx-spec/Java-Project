package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.TantouDeadlineRes;
import com.mangakousei.mangakousei_backend.entity.entity.ChapterPageDeadline;
import com.mangakousei.mangakousei_backend.repository.ChapterPageDeadlineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TantouDashboardService {

    private final ChapterPageDeadlineRepository deadlineRepository;
    private static final DateTimeFormatter VI_DATE = DateTimeFormatter.ofPattern("dd/MM");

    @Transactional(readOnly = true)
    public List<TantouDeadlineRes> getDeadlineAlerts(Long tantouId) {
        LocalDate today = LocalDate.now();
        LocalDate soon  = today.plusDays(3);

        return deadlineRepository
                .findUpcomingByTantouId(tantouId, soon)
                .stream()
                .filter(d -> !"approved".equals(d.getStatus()))
                .map(d -> toRes(d, today))
                .sorted((a, b) -> {
                    int orderA = order(a.getLabelType());
                    int orderB = order(b.getLabelType());
                    return Integer.compare(orderA, orderB);
                })
                .collect(Collectors.toList());
    }

    private TantouDeadlineRes toRes(ChapterPageDeadline d, LocalDate today) {
        LocalDate due = d.getDueDate() != null ? d.getDueDate() : today;

        String labelType, label, timeTag;

        if (due.isBefore(today)) {
            labelType = "overdue";
            label     = "QUÁ HẠN";
            long daysAgo = today.toEpochDay() - due.toEpochDay();
            timeTag = daysAgo == 1 ? "Hôm qua" : daysAgo + " ngày trước";
        } else if (due.isEqual(today)) {
            labelType = "due";
            label     = "ĐẾN HẠN";
            timeTag   = "Hôm nay";
        } else {
            labelType = "soon";
            label     = "SẮP ĐẾN";
            long daysLeft = due.toEpochDay() - today.toEpochDay();
            timeTag = daysLeft == 1 ? "Ngày mai" : due.format(VI_DATE);
        }

        var chapter = d.getChapter();
        var series  = chapter != null ? chapter.getSeries() : null;
        String mangakaName = series != null && series.getCreator() != null
                ? series.getCreator().getFullName() : "—";
        String seriesTitle = series != null ? series.getTitle() : "—";
        String chTitle = "Trang " + d.getPageFrom() + "–" + d.getPageTo()
                + (chapter != null ? " – Ch." + chapter.getChapterNumber() : "");

        return TantouDeadlineRes.builder()
                .deadlineId(d.getDeadlineId())
                .labelType(labelType)
                .label(label)
                .timeTag(timeTag)
                .title(chTitle)
                .author(mangakaName)
                .series(seriesTitle)
                .dueDate(due)
                .build();
    }

    private int order(String labelType) {
        return switch (labelType) {
            case "overdue" -> 0;
            case "due"     -> 1;
            default        -> 2;
        };
    }
}