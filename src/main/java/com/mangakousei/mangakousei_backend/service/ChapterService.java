package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.CreateChapterReq;
import com.mangakousei.mangakousei_backend.dto.request.ReviewPageGroupReq;
import com.mangakousei.mangakousei_backend.dto.request.SetPageDeadlineReq;
import com.mangakousei.mangakousei_backend.dto.response.ChapterRes;
import com.mangakousei.mangakousei_backend.dto.response.PageDeadlineRes;
import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.entity.status.ChapterStatus;
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

        return toRes(chapterRepository.save(chapter));
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

        return toDeadlineRes(deadlineRepository.save(deadline));
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
        } else {
            long total = deadlineRepository.countByChapterChapterId(chapter.getChapterId());
            long approved = deadlineRepository.countByChapterChapterIdAndStatus(
                    chapter.getChapterId(), "approved");
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
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new CustomAppException(
                        "User not found", HttpStatus.NOT_FOUND));
    }
}