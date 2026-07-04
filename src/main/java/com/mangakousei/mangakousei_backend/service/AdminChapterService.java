package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.constant.RealtimeQueues;
import com.mangakousei.mangakousei_backend.dto.request.AdminReviewChapterReq;
import com.mangakousei.mangakousei_backend.dto.request.LogContext;
import com.mangakousei.mangakousei_backend.dto.response.ChapterRes;
import com.mangakousei.mangakousei_backend.dto.response.PageDeadlineRes;
import com.mangakousei.mangakousei_backend.entity.entity.Chapter;
import com.mangakousei.mangakousei_backend.entity.entity.ChapterPageDeadline;
import com.mangakousei.mangakousei_backend.entity.entity.Series;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.entity.status.ChapterStatus;
import com.mangakousei.mangakousei_backend.entity.type.ActionType;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.ChapterPageDeadlineRepository;
import com.mangakousei.mangakousei_backend.repository.ChapterRepository;
import com.mangakousei.mangakousei_backend.repository.ChapterStatusRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final UserRepository userRepository;
    private final RealtimePushService realtimePushService;

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
                .orElseThrow(() -> new CustomAppException("Không tìm thấy chapter", HttpStatus.NOT_FOUND));

        String currentStatus = chapter.getChapterStatus() != null
                ? chapter.getChapterStatus().getChapterStatusName() : "";
        if (!"pending_publish".equals(currentStatus)) {
            throw new CustomAppException("Chapter phải ở trạng thái 'pending_publish'", HttpStatus.BAD_REQUEST);
        }

        boolean approved = "approved".equals(req.getDecision());
        Series series = chapter.getSeries();

        if (approved) {
            ChapterStatus published = chapterStatusRepository.findByChapterStatusName("published")
                    .orElseThrow(() -> new CustomAppException(
                            "Không tìm thấy ChapterStatus 'published'", HttpStatus.INTERNAL_SERVER_ERROR));
            chapter.setChapterStatus(published);
            chapter.setAdminNote(null);
            chapterRepository.save(chapter);
        } else {
            ChapterStatus inProgress = chapterStatusRepository.findByChapterStatusName("in_progress")
                    .orElseThrow(() -> new CustomAppException(
                            "Không tìm thấy ChapterStatus 'in_progress'", HttpStatus.INTERNAL_SERVER_ERROR));
            chapter.setChapterStatus(inProgress);
            chapter.setAdminNote(req.getNote());
            chapterRepository.save(chapter);

            String noteForMangaka = "[Admin yêu cầu sửa] " + (req.getNote() != null ? req.getNote() : "");
            List<ChapterPageDeadline> deadlines = deadlineRepository
                    .findByChapterChapterIdOrderByPageFrom(chapterId);
            for (ChapterPageDeadline d : deadlines) {
                d.setStatus("revision");
                d.setReviewNote(noteForMangaka);
                d.setReviewedAt(LocalDateTime.now());
                deadlineRepository.save(d);

                realtimePushService.pushToUser(
                        series.getCreator().getEmail(), RealtimeQueues.PAGE_DEADLINE_UPDATES, toDeadlineRes(d));
                realtimePushService.pushToUser(
                        series.getEditor().getEmail(), RealtimeQueues.PAGE_DEADLINE_UPDATES, toDeadlineRes(d));
            }
        }

        ChapterRes result = toResWithSeries(chapter);

        activityLogService.log(LogContext.builder()
                .actionType(approved ? ActionType.ADMIN_REVIEW_APPROVED : ActionType.ADMIN_REVIEW_REVISION)
                .detail((approved ? "Admin duyệt đăng" : "Admin yêu cầu sửa, trả về Mangaka")
                        + " Ch." + chapter.getChapterNumber()
                        + (series != null ? " | " + series.getTitle() : ""))
                .entityType("CHAPTER")
                .entityId(chapterId)
                .seriesId(series != null ? series.getSeriesId() : null)
                .chapterId(chapterId)
                .build());

        if (series != null && series.getCreator() != null) {
            Long mangakaId = series.getCreator().getUserId();
            notificationService.send(mangakaId, "REVIEW",
                    approved ? "🎉 Chapter của bạn được duyệt đăng!" : "✏️ Admin yêu cầu chỉnh sửa chapter",
                    approved
                            ? "Ch." + chapter.getChapterNumber() + " | " + series.getTitle() + " đã được duyệt đăng!"
                            : "Admin yêu cầu chỉnh sửa Ch." + chapter.getChapterNumber() + " | " + series.getTitle()
                              + (req.getNote() != null ? ". Ghi chú: " + req.getNote() : "")
                              + ". Vui lòng nộp lại các nhóm trang.");
            realtimePushService.pushToUser(series.getCreator().getEmail(), RealtimeQueues.CHAPTER_UPDATES, result);
        }

        if (series != null && series.getEditor() != null) {
            Long tantouId = series.getEditor().getUserId();
            notificationService.send(tantouId, "REVIEW",
                    approved ? "✅ Chapter được duyệt đăng" : "ℹ️ Admin trả chapter về Mangaka",
                    "Ch." + chapter.getChapterNumber() + " | " + series.getTitle()
                            + (approved ? " đã được Admin duyệt đăng."
                            : " đã được Admin yêu cầu Mangaka chỉnh sửa lại. Bạn sẽ review lại khi Mangaka nộp."));
            realtimePushService.pushToUser(series.getEditor().getEmail(), RealtimeQueues.CHAPTER_UPDATES, result);
        }

        List<User> allAdmins = userRepository.findAllByRoleName("ADMIN");
        for (User a : allAdmins) {
            realtimePushService.pushToUser(a.getEmail(), RealtimeQueues.ADMIN_CHAPTER_UPDATES, result);
        }

        return result;
    }

    private PageDeadlineRes toDeadlineRes(ChapterPageDeadline d) {
        return PageDeadlineRes.builder()
                .deadlineId(d.getDeadlineId())
                .pageFrom(d.getPageFrom())
                .pageTo(d.getPageTo())
                .dueDate(d.getDueDate())
                .status(d.getStatus())
                .submittedAt(d.getSubmittedAt())
                .setByName(d.getSetBy() != null ? d.getSetBy().getFullName() : null)
                .reviewedAt(d.getReviewedAt())
                .reviewNote(d.getReviewNote())
                .build();
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