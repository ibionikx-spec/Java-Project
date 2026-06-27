package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.AdminReviewChapterReq;
import com.mangakousei.mangakousei_backend.dto.request.LogContext;
import com.mangakousei.mangakousei_backend.dto.response.ChapterRes;
import com.mangakousei.mangakousei_backend.dto.response.PageDeadlineRes;
import com.mangakousei.mangakousei_backend.entity.entity.Chapter;
import com.mangakousei.mangakousei_backend.entity.status.ChapterStatus;
import com.mangakousei.mangakousei_backend.entity.type.ActionType;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.ChapterPageDeadlineRepository;
import com.mangakousei.mangakousei_backend.repository.ChapterRepository;
import com.mangakousei.mangakousei_backend.repository.ChapterStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminChapterService {

    private final ChapterRepository chapterRepository;
    private final ChapterStatusRepository chapterStatusRepository;
    private final ChapterPageDeadlineRepository deadlineRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public List<ChapterRes> getPendingPublishChapters() {
        return chapterRepository
                .findByChapterStatusChapterStatusNameOrderByCreatedAtDesc("pending_publish")
                .stream()
                .map(this::toResWithSeries)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChapterRes reviewChapter(Long chapterId, AdminReviewChapterReq req) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy chapter", HttpStatus.NOT_FOUND));

        String currentStatus = chapter.getChapterStatus() != null
                ? chapter.getChapterStatus().getChapterStatusName() : "";

        if (!"pending_publish".equals(currentStatus)) {
            throw new CustomAppException(
                    "Chapter phải ở trạng thái 'pending_publish'",
                    HttpStatus.BAD_REQUEST);
        }

        boolean approved = "approved".equals(req.getDecision());

        if (approved) {
            ChapterStatus published = chapterStatusRepository
                    .findByChapterStatusName("published")
                    .orElseThrow(() -> new CustomAppException(
                            "Không tìm thấy ChapterStatus 'published'",
                            HttpStatus.INTERNAL_SERVER_ERROR));
            chapter.setChapterStatus(published);
            chapter.setAdminNote(null);
        } else {
            chapter.setAdminNote(req.getNote());
        }

        ChapterRes result = toResWithSeries(chapterRepository.save(chapter));

        var series = chapter.getSeries();
        activityLogService.log(LogContext.builder()
                .actionType(approved ? ActionType.ADMIN_REVIEW_APPROVED : ActionType.ADMIN_REVIEW_REVISION)
                .detail((approved ? "Admin duyệt đăng" : "Admin yêu cầu chỉnh sửa")
                        + " Ch." + chapter.getChapterNumber()
                        + (chapter.getTitle() != null ? " – " + chapter.getTitle() : "")
                        + (series != null ? " | " + series.getTitle() : ""))
                .entityType("CHAPTER")
                .entityId(chapterId)
                .seriesId(series != null ? series.getSeriesId() : null)
                .chapterId(chapterId)
                .build());

        if (series != null && series.getEditor() != null) {
              notificationService.send(series.getEditor().getUserId(), "REVIEW",
                      approved ? "✅ Chapter được duyệt đăng" : "✏️ Chapter cần chỉnh sửa",
                      (approved ? "Admin đã duyệt đăng" : "Admin yêu cầu chỉnh sửa")
                      + " Ch." + chapter.getChapterNumber()
                      + (chapter.getTitle() != null ? " – " + chapter.getTitle() : "")
                      + " | " + series.getTitle()
                      + (!approved && req.getNote() != null ? ": " + req.getNote() : ""));
        }

        if (series != null && series.getCreator() != null) {
              notificationService.send(series.getCreator().getUserId(), "REVIEW",
                      approved ? "🎉 Chapter của bạn được duyệt đăng!" : "✏️ Chapter cần chỉnh sửa",
                      (approved ? "Admin đã duyệt đăng" : "Admin yêu cầu chỉnh sửa")
                      + " Ch." + chapter.getChapterNumber()
                      + (chapter.getTitle() != null ? " – " + chapter.getTitle() : "")
                      + " | " + series.getTitle()
                      + (!approved && req.getNote() != null ? ". Ghi chú: " + req.getNote() : ""));
        }

        return result;
    }

    private ChapterRes toResWithSeries(Chapter c) {
        List<PageDeadlineRes> deadlines = deadlineRepository
                .findByChapterChapterIdOrderByPageFrom(c.getChapterId())
                .stream()
                .map(d -> PageDeadlineRes.builder()
                        .deadlineId(d.getDeadlineId())
                        .pageFrom(d.getPageFrom())
                        .pageTo(d.getPageTo())
                        .dueDate(d.getDueDate())
                        .status(d.getStatus())
                        .submittedAt(d.getSubmittedAt())
                        .reviewedAt(d.getReviewedAt())
                        .reviewNote(d.getReviewNote())
                        .build())
                .collect(Collectors.toList());

        var series = c.getSeries();
        return ChapterRes.builder()
                .chapterId(c.getChapterId())
                .chapterNumber(c.getChapterNumber())
                .title(c.getTitle())
                .chapterStatus(c.getChapterStatus() != null
                        ? c.getChapterStatus().getChapterStatusName() : null)
                .deadline(c.getDeadline())
                .createdAt(c.getCreatedAt())
                .pageDeadlines(deadlines)
                .totalDeadlines(deadlines.size())
                .submittedDeadlines(0)
                .adminNote(c.getAdminNote())
                .seriesId(series != null ? series.getSeriesId() : null)
                .seriesTitle(series != null ? series.getTitle() : null)
                .mangakaName(series != null && series.getCreator() != null
                        ? series.getCreator().getFullName() : null)
                .mangakaAvatarUrl(series != null && series.getCreator() != null
                        ? series.getCreator().getAvatarUrl() : null)
                .build();
    }
}