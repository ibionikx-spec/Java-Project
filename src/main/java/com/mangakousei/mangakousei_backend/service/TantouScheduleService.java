package com.mangakousei.mangakousei_backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mangakousei.mangakousei_backend.dto.response.ScheduleEventResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TantouScheduleService {

    @PersistenceContext
    private EntityManager entityManager;
    @Transactional(readOnly = true)
    public List<ScheduleEventResponse> getScheduleEvents(Long tantouId, LocalDate from, LocalDate to, String status) {
        log.info("Fetching schedule for tantouId={}, from={}, to={}, status={}", tantouId, from, to, status);
        StringBuilder jpqlChapter = new StringBuilder(
            "SELECT c.chapterId, 'chapter', c.deadline, c.series.title, c.series.seriesId, c.chapterId, c.chapterNumber, c.chapterStatus.chapterStatusName " +
            "FROM Chapter c WHERE c.series.editor.userId = :tantouId AND c.deadline IS NOT NULL"
        );
        if (from != null) {
            jpqlChapter.append(" AND c.deadline >= :from");
        }
        if (to != null) {
            jpqlChapter.append(" AND c.deadline <= :to");
        }
        if (status != null && !status.isBlank()) {
            jpqlChapter.append(" AND c.chapterStatus.chapterStatusName = :status");
        }
        TypedQuery<Object[]> queryChapter = entityManager.createQuery(jpqlChapter.toString(), Object[].class);
        queryChapter.setParameter("tantouId", tantouId);
        if (from != null) {
            queryChapter.setParameter("from", from.atStartOfDay());
        }
        if (to != null) {
            queryChapter.setParameter("to", to.atTime(LocalTime.MAX));
        }
        if (status != null && !status.isBlank()) {
            queryChapter.setParameter("status", status);
        }
        List<Object[]> chapterRows = queryChapter.getResultList();
        log.debug("Chapter rows: {}", chapterRows.size());
        StringBuilder jpqlPage = new StringBuilder(
            "SELECT cpd.deadlineId, 'page', cpd.dueDate, cpd.chapter.series.title, cpd.chapter.series.seriesId, cpd.chapter.chapterId, cpd.chapter.chapterNumber, cpd.status, cpd.pageFrom, cpd.pageTo " +
            "FROM ChapterPageDeadline cpd WHERE cpd.chapter.series.editor.userId = :tantouId"
        );
        if (from != null) {
            jpqlPage.append(" AND cpd.dueDate >= :from");
        }
        if (to != null) {
            jpqlPage.append(" AND cpd.dueDate <= :to");
        }
        if (status != null && !status.isBlank()) {
            jpqlPage.append(" AND cpd.status = :status");
        }
        TypedQuery<Object[]> queryPage = entityManager.createQuery(jpqlPage.toString(), Object[].class);
        queryPage.setParameter("tantouId", tantouId);
        if (from != null) {
            queryPage.setParameter("from", from);
        }
        if (to != null) {
            queryPage.setParameter("to", to);
        }
        if (status != null && !status.isBlank()) {
            queryPage.setParameter("status", status);
        }
        List<Object[]> pageRows = queryPage.getResultList();
        log.debug("Page rows: {}", pageRows.size());
        List<ScheduleEventResponse> events = new ArrayList<>();
        events.addAll(convertChapterResults(chapterRows));
        events.addAll(convertPageResults(pageRows));
        events.sort(Comparator.comparing(ScheduleEventResponse::getDate));
        return events;
    }
    private List<ScheduleEventResponse> convertChapterResults(List<Object[]> rows) {
        List<ScheduleEventResponse> list = new ArrayList<>();
        for (Object[] row : rows) {
            try {
                Long eventId = ((Number) row[0]).longValue();
                String type = (String) row[1];
                LocalDateTime dateTime = (LocalDateTime) row[2];
                LocalDate date = dateTime.toLocalDate();
                String seriesTitle = (String) row[3];
                Long seriesId = ((Number) row[4]).longValue();
                Long chapterId = ((Number) row[5]).longValue();
                Integer chapterNumber = ((Number) row[6]).intValue();
                String status = (String) row[7];

                String title = "Hạn nộp chương " + chapterNumber;
                list.add(new ScheduleEventResponse(eventId, type, date, seriesTitle, seriesId, chapterId, chapterNumber, status, title, null, null));
            } catch (Exception e) {
                log.error("Error converting chapter row: {}", (Object) row, e);
            }
        }
        return list;
    }
    private List<ScheduleEventResponse> convertPageResults(List<Object[]> rows) {
        List<ScheduleEventResponse> list = new ArrayList<>();
        for (Object[] row : rows) {
            try {
                Long eventId = ((Number) row[0]).longValue();
                String type = (String) row[1];
                LocalDate date = (LocalDate) row[2];
                String seriesTitle = (String) row[3];
                Long seriesId = ((Number) row[4]).longValue();
                Long chapterId = ((Number) row[5]).longValue();
                Integer chapterNumber = ((Number) row[6]).intValue();
                String status = (String) row[7];
                Integer pageFrom = row[8] != null ? ((Number) row[8]).intValue() : null;
                Integer pageTo = row[9] != null ? ((Number) row[9]).intValue() : null;

                String title = (pageFrom != null && pageTo != null)
                        ? "Hạn nộp trang " + pageFrom + " - " + pageTo
                        : "Hạn nộp trang";

                list.add(new ScheduleEventResponse(eventId, type, date, seriesTitle, seriesId, chapterId, chapterNumber, status, title, pageFrom, pageTo));
            } catch (Exception e) {
                log.error("Error converting page row: {}", (Object) row, e);
            }
        }
        return list;
    }
}