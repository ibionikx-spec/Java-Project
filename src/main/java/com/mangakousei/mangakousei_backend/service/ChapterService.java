package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.CreateChapterReq;
import com.mangakousei.mangakousei_backend.dto.request.LogContext;
import com.mangakousei.mangakousei_backend.dto.request.ReviewPageGroupReq;
import com.mangakousei.mangakousei_backend.dto.request.SetPageDeadlineReq;
import com.mangakousei.mangakousei_backend.dto.response.ChapterRes;
import com.mangakousei.mangakousei_backend.dto.response.PageDeadlineRes;
import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.entity.status.ChapterStatus;
import com.mangakousei.mangakousei_backend.entity.type.ActionType;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final SeriesRepository seriesRepository;
    private final ChapterStatusRepository chapterStatusRepository;
    private final ChapterPageDeadlineRepository deadlineRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public List<ChapterRes> getChaptersBySeries(Long seriesId) {
        return chapterRepository
                .findBySeriesSeriesIdOrderByChapterNumberAsc(seriesId)
                .stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    public List<ChapterRes> getSubmittedChaptersForTantou() {
        User tantou = getCurrentUser();
        return chapterRepository
                .findBySeriesEditorUserIdAndChapterStatusChapterStatusNameOrderByCreatedAtDesc(
                        tantou.getUserId(), "pages_submitted")
                .stream()
                .map(this::toResWithSeries)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChapterRes createChapter(CreateChapterReq req) {
        User mangaka = getCurrentUser();

        Series series = seriesRepository.findById(req.getSeriesId())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy series", HttpStatus.NOT_FOUND));

        if (!series.getCreator().getUserId().equals(mangaka.getUserId())) {
            throw new CustomAppException(
                    "Bạn không có quyền tạo chapter cho series này",
                    HttpStatus.FORBIDDEN);
        }

        chapterRepository.findBySeriesSeriesIdAndChapterNumber(
                        req.getSeriesId(), req.getChapterNumber())
                .ifPresent(c -> { throw new CustomAppException(
                        "Chapter " + req.getChapterNumber() + " đã tồn tại",
                        HttpStatus.CONFLICT); });

        ChapterStatus draftStatus = chapterStatusRepository
                .findByChapterStatusName("draft")
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy ChapterStatus 'draft'",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        Chapter chapter = Chapter.builder()
                .series(series)
                .chapterNumber(req.getChapterNumber())
                .title(req.getTitle())
                .chapterStatus(draftStatus)
                .build();

        Chapter saved = chapterRepository.save(chapter);

        activityLogService.log(LogContext.builder()
                .actionType(ActionType.CREATE_CHAPTER)
                .detail("Tạo Chapter " + saved.getChapterNumber()
                        + (saved.getTitle() != null ? " – " + saved.getTitle() : "")
                        + " | Series: " + series.getTitle())
                .entityType("CHAPTER")
                .entityId(saved.getChapterId())
                .seriesId(series.getSeriesId())
                .chapterId(saved.getChapterId())
                .build());

        return toRes(saved);
    }

    @Transactional
    public PageDeadlineRes setPageDeadline(Long chapterId, SetPageDeadlineReq req) {
        User tantou = getCurrentUser();

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy chapter", HttpStatus.NOT_FOUND));

        if (req.getPageFrom() > req.getPageTo()) {
            throw new CustomAppException(
                    "pageFrom không được lớn hơn pageTo", HttpStatus.BAD_REQUEST);
        }

        ChapterPageDeadline deadline = ChapterPageDeadline.builder()
                .chapter(chapter)
                .pageFrom(req.getPageFrom())
                .pageTo(req.getPageTo())
                .dueDate(req.getDueDate())
                .setBy(tantou)
                .status("pending")
                .build();

        ChapterPageDeadline saved = deadlineRepository.save(deadline);

        if ("draft".equals(chapter.getChapterStatus().getChapterStatusName())) {
            ChapterStatus inProgress = chapterStatusRepository
                    .findByChapterStatusName("in_progress")
                    .orElseThrow();
            chapter.setChapterStatus(inProgress);
            chapterRepository.save(chapter);
        }

        Series series = chapter.getSeries();
        activityLogService.log(LogContext.builder()
                .actionType(ActionType.CREATE_PAGE_DEADLINE)
                .detail("Tạo deadline trang " + req.getPageFrom() + "–" + req.getPageTo()
                        + " | Ch." + chapter.getChapterNumber()
                        + " – " + (series != null ? series.getTitle() : ""))
                .entityType("PAGE_DEADLINE")
                .entityId(saved.getDeadlineId())
                .seriesId(series != null ? series.getSeriesId() : null)
                .chapterId(chapter.getChapterId())
                .build());

        return toDeadlineRes(saved);
    }

    @Transactional
    public PageDeadlineRes updatePageDeadline(Long deadlineId, SetPageDeadlineReq req) {
        ChapterPageDeadline deadline = deadlineRepository.findById(deadlineId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy deadline", HttpStatus.NOT_FOUND));

        if ("submitted".equals(deadline.getStatus()) || "approved".equals(deadline.getStatus())) {
            throw new CustomAppException(
                    "Không thể sửa deadline đã được nộp hoặc duyệt", HttpStatus.BAD_REQUEST);
        }

        if (req.getPageFrom() > req.getPageTo()) {
            throw new CustomAppException(
                    "pageFrom không được lớn hơn pageTo", HttpStatus.BAD_REQUEST);
        }

        deadline.setPageFrom(req.getPageFrom());
        deadline.setPageTo(req.getPageTo());
        deadline.setDueDate(req.getDueDate());

        ChapterPageDeadline saved = deadlineRepository.save(deadline);

        Chapter chapter = deadline.getChapter();
        Series series = chapter != null ? chapter.getSeries() : null;
        activityLogService.log(LogContext.builder()
                .actionType(ActionType.UPDATE_PAGE_DEADLINE)
                .detail("Cập nhật deadline trang " + req.getPageFrom() + "–" + req.getPageTo()
                        + (chapter != null ? " | Ch." + chapter.getChapterNumber() : ""))
                .entityType("PAGE_DEADLINE")
                .entityId(deadlineId)
                .seriesId(series != null ? series.getSeriesId() : null)
                .chapterId(chapter != null ? chapter.getChapterId() : null)
                .build());

        return toDeadlineRes(saved);
    }

    @Transactional
    public void deletePageDeadline(Long deadlineId) {
        ChapterPageDeadline deadline = deadlineRepository.findById(deadlineId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy deadline", HttpStatus.NOT_FOUND));

        if ("submitted".equals(deadline.getStatus()) || "approved".equals(deadline.getStatus())) {
            throw new CustomAppException(
                    "Không thể xoá deadline đã được nộp hoặc duyệt", HttpStatus.BAD_REQUEST);
        }

        deadlineRepository.delete(deadline);
    }

    @Transactional
    public PageDeadlineRes submitPageGroup(Long deadlineId) {
        ChapterPageDeadline deadline = deadlineRepository.findById(deadlineId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy deadline", HttpStatus.NOT_FOUND));

        String currentStatus = deadline.getStatus();
        if ("submitted".equals(currentStatus) || "approved".equals(currentStatus)) {
            throw new CustomAppException(
                    "Nhóm trang này đã được nộp hoặc duyệt rồi", HttpStatus.BAD_REQUEST);
        }

        boolean isResubmit = "revision".equals(currentStatus);

        deadline.setStatus("submitted");
        deadline.setSubmittedAt(LocalDateTime.now());
        deadline.setReviewedAt(null);
        deadline.setReviewNote(null);
        deadlineRepository.save(deadline);

        Chapter chapter = deadline.getChapter();
        long total = deadlineRepository.countByChapterChapterId(chapter.getChapterId());
        long submitted = deadlineRepository.countByChapterChapterIdAndStatus(
                chapter.getChapterId(), "submitted");

        if (total > 0 && total == submitted) {
            chapterStatusRepository.findByChapterStatusName("pages_submitted")
                    .ifPresent(status -> {
                        chapter.setChapterStatus(status);
                        chapterRepository.save(chapter);
                    });
        }

        Series series = chapter.getSeries();
        activityLogService.log(LogContext.builder()
                .actionType(isResubmit ? ActionType.RESUBMIT_PAGES : ActionType.SUBMIT_PAGES)
                .detail((isResubmit ? "Nộp lại" : "Nộp") + " nhóm trang "
                        + deadline.getPageFrom() + "–" + deadline.getPageTo()
                        + " | Ch." + chapter.getChapterNumber()
                        + " – " + (series != null ? series.getTitle() : ""))
                .entityType("PAGE_DEADLINE")
                .entityId(deadlineId)
                .seriesId(series != null ? series.getSeriesId() : null)
                .chapterId(chapter.getChapterId())
                .build());

        if (series != null && series.getEditor() != null)
            notificationService.send(series.getEditor().getUserId(), "REVIEW",
                    isResubmit ? "🔄 Mangaka đã nộp lại trang" : "📥 Mangaka vừa nộp trang",
                    (isResubmit ? "Mangaka đã nộp lại" : "Mangaka vừa nộp")
                            + " nhóm trang " + deadline.getPageFrom() + "–" + deadline.getPageTo()
                            + " | Ch." + chapter.getChapterNumber()
                            + " – " + series.getTitle() + ". Hãy xem xét!");

        return toDeadlineRes(deadline);
    }

    @Transactional
    public PageDeadlineRes reviewPageGroup(Long deadlineId, ReviewPageGroupReq req) {
        ChapterPageDeadline deadline = deadlineRepository.findById(deadlineId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy deadline", HttpStatus.NOT_FOUND));

        if (!"submitted".equals(deadline.getStatus())) {
            throw new CustomAppException(
                    "Chỉ có thể review nhóm trang đã nộp (status = submitted)",
                    HttpStatus.BAD_REQUEST);
        }

        boolean approved = "approved".equals(req.getDecision());
        deadline.setStatus(req.getDecision());
        deadline.setReviewedAt(LocalDateTime.now());
        deadline.setReviewNote(req.getNote());
        deadlineRepository.save(deadline);

        Chapter chapter = deadline.getChapter();

        if ("revision".equals(req.getDecision())) {
            chapterStatusRepository.findByChapterStatusName("in_progress")
                    .ifPresent(s -> {
                        chapter.setChapterStatus(s);
                        chapterRepository.save(chapter);
                    });
        }

        Series series = chapter != null ? chapter.getSeries() : null;
        activityLogService.log(LogContext.builder()
                .actionType(approved ? ActionType.REVIEW_APPROVED : ActionType.REVIEW_REVISION)
                .detail((approved ? "Duyệt" : "Yêu cầu chỉnh sửa") + " nhóm trang "
                        + deadline.getPageFrom() + "–" + deadline.getPageTo()
                        + (chapter != null ? " | Ch." + chapter.getChapterNumber() : "")
                        + (series != null ? " – " + series.getTitle() : ""))
                .entityType("PAGE_DEADLINE")
                .entityId(deadlineId)
                .seriesId(series != null ? series.getSeriesId() : null)
                .chapterId(chapter != null ? chapter.getChapterId() : null)
                .build());

        assert chapter != null;
        if (chapter.getSeries() != null && chapter.getSeries().getCreator() != null) {
          Long mangakaId = chapter.getSeries().getCreator().getUserId();
          String seriesTitle = chapter.getSeries().getTitle();
          if (approved) {
              notificationService.send(mangakaId, "REVIEW",
                  "✅ Nhóm trang được duyệt",
                  "Tantou đã duyệt nhóm trang " + deadline.getPageFrom() + "–" + deadline.getPageTo()
                  + " trong Ch." + chapter.getChapterNumber() + " – " + seriesTitle);
          } else {
              notificationService.send(mangakaId, "REVIEW",
                  "✏️ Yêu cầu chỉnh sửa trang",
                  "Tantou yêu cầu chỉnh sửa nhóm trang " + deadline.getPageFrom() + "–" + deadline.getPageTo()
                  + " trong Ch." + chapter.getChapterNumber() + " – " + seriesTitle);
          }
      }

        return toDeadlineRes(deadline);
    }

    @Transactional
    public ChapterRes submitChapterToAdmin(Long chapterId) {
        User tantou = getCurrentUser();

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy chapter", HttpStatus.NOT_FOUND));

        if (chapter.getSeries().getEditor() == null ||
                !chapter.getSeries().getEditor().getUserId().equals(tantou.getUserId())) {
            throw new CustomAppException(
                    "Bạn không có quyền submit chapter này", HttpStatus.FORBIDDEN);
        }

        String currentStatus = chapter.getChapterStatus() != null
                ? chapter.getChapterStatus().getChapterStatusName() : "";

        if (!"pages_submitted".equals(currentStatus)) {
            throw new CustomAppException(
                    "Chapter phải ở trạng thái 'pages_submitted' để submit lên admin",
                    HttpStatus.BAD_REQUEST);
        }

        boolean hasUnreviewed = deadlineRepository
                .existsByChapterChapterIdAndStatus(chapterId, "submitted");
        if (hasUnreviewed) {
            throw new CustomAppException(
                    "Vẫn còn nhóm trang chưa được review. Hãy duyệt tất cả trước khi submit.",
                    HttpStatus.BAD_REQUEST);
        }

        boolean hasRevision = deadlineRepository
                .existsByChapterChapterIdAndStatus(chapterId, "revision");
        if (hasRevision) {
            throw new CustomAppException(
                    "Vẫn còn nhóm trang yêu cầu sửa lại. Mangaka cần nộp lại trước.",
                    HttpStatus.BAD_REQUEST);
        }

        ChapterStatus pendingPublish = chapterStatusRepository
                .findByChapterStatusName("pending_publish")
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy ChapterStatus 'pending_publish'",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        chapter.setChapterStatus(pendingPublish);
        chapterRepository.save(chapter);

        Series series = chapter.getSeries();
        activityLogService.log(LogContext.builder()
                .actionType(ActionType.SUBMIT_CHAPTER_TO_ADMIN)
                .detail("Gửi Ch." + chapter.getChapterNumber()
                        + (chapter.getTitle() != null ? " – " + chapter.getTitle() : "")
                        + " lên Admin duyệt"
                        + (series != null ? " | " + series.getTitle() : ""))
                .entityType("CHAPTER")
                .entityId(chapterId)
                .seriesId(series != null ? series.getSeriesId() : null)
                .chapterId(chapterId)
                .build());

        if (chapter.getSeries() != null && chapter.getSeries().getEditor() != null) {
          Long tantouId = chapter.getSeries().getEditor().getUserId();
          notificationService.send(tantouId, "REVIEW",
              "📬 Chapter chờ duyệt Admin",
              "Ch." + chapter.getChapterNumber()
              + (chapter.getTitle() != null ? " – " + chapter.getTitle() : "")
              + " | " + chapter.getSeries().getTitle() + " đã được gửi lên Admin.");
      }

        return toResWithSeries(chapter);
    }

    private ChapterRes toRes(Chapter c) {
        List<PageDeadlineRes> deadlines = deadlineRepository
                .findByChapterChapterIdOrderByPageFrom(c.getChapterId())
                .stream()
                .map(this::toDeadlineRes)
                .collect(Collectors.toList());

        long total = deadlines.size();
        long submitted = deadlines.stream()
                .filter(d -> "submitted".equals(d.getStatus()))
                .count();

        return ChapterRes.builder()
                .chapterId(c.getChapterId())
                .chapterNumber(c.getChapterNumber())
                .title(c.getTitle())
                .chapterStatus(c.getChapterStatus() != null
                        ? c.getChapterStatus().getChapterStatusName() : null)
                .deadline(c.getDeadline())
                .createdAt(c.getCreatedAt())
                .pageDeadlines(deadlines)
                .totalDeadlines(total)
                .submittedDeadlines(submitted)
                .adminNote(c.getAdminNote())
                .build();
    }

    private ChapterRes toResWithSeries(Chapter c) {
        ChapterRes base = toRes(c);
        Series series = c.getSeries();
        if (series != null) {
            base.setSeriesId(series.getSeriesId());
            base.setSeriesTitle(series.getTitle());
            if (series.getCreator() != null) {
                base.setMangakaName(series.getCreator().getFullName());
                base.setMangakaAvatarUrl(series.getCreator().getAvatarUrl());
            }
        }
        return base;
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

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new CustomAppException(
                        "User not found", HttpStatus.NOT_FOUND));
    }
}