package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.constant.RealtimeQueues;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private final RealtimePushService realtimePushService;
    private final PageRepository pageRepository;

    public byte[] downloadChapterFiles(Long chapterId) {
        User mangaka = getCurrentUser();

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy chapter", HttpStatus.NOT_FOUND));

        Series series = chapter.getSeries();
        if (series == null || series.getCreator() == null
                || !series.getCreator().getUserId().equals(mangaka.getUserId())) {
            throw new CustomAppException(
                    "Bạn không có quyền tải file chapter này", HttpStatus.FORBIDDEN);
        }

        List<Page> pages = pageRepository
                .findByChapterChapterIdOrderByPageNumberAsc(chapterId);

        if (pages.isEmpty()) {
            throw new CustomAppException(
                    "Chapter chưa có trang nào để tải", HttpStatus.BAD_REQUEST);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean hasAnyFile = false;

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Page page : pages) {
                String fileUrl = page.getFileUrl();
                if (fileUrl == null || fileUrl.isBlank()) continue;

                try {
                    byte[] imageBytes = downloadBytes(fileUrl);
                    String ext = extractExtension(fileUrl);
                    String entryName = "page_"
                            + String.format("%03d", page.getPageNumber())
                            + "." + ext;

                    zos.putNextEntry(new ZipEntry(entryName));
                    zos.write(imageBytes);
                    zos.closeEntry();
                    hasAnyFile = true;
                } catch (IOException e) {
                        //
                }
            }
        } catch (IOException e) {
            throw new CustomAppException(
                    "Lỗi khi tạo file zip", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!hasAnyFile) {
            throw new CustomAppException(
                    "Chapter chưa có ảnh trang nào để tải", HttpStatus.BAD_REQUEST);
        }

        return baos.toByteArray();
    }

    private byte[] downloadBytes(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream()) {
            return in.readAllBytes();
        }
    }

    private String extractExtension(String fileUrl) {
        String path = fileUrl.split("\\?")[0];
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx == -1 || dotIdx == path.length() - 1) return "jpg";
        String ext = path.substring(dotIdx + 1).toLowerCase();
        return ext.length() <= 5 ? ext : "jpg";
    }

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

        if (series.getEditor() != null) {
            notificationService.send(series.getEditor().getUserId(), "SYSTEM",
                    "📖 Chapter mới được tạo",
                    mangaka.getFullName() + " vừa tạo Ch." + saved.getChapterNumber()
                            + " trong series " + series.getTitle() + ". Hãy set deadline cho nhóm trang.");

            realtimePushService.pushToUser(
                    series.getEditor().getEmail(),
                    RealtimeQueues.CHAPTER_UPDATES,
                    toResWithSeries(saved)
            );
        }

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

        if (series != null && series.getCreator() != null) {
            notificationService.send(series.getCreator().getUserId(), "SYSTEM",
                    "📅 Deadline mới được set",
                    "Tantou vừa set deadline cho nhóm trang " + req.getPageFrom() + "–" + req.getPageTo()
                            + " | Ch." + chapter.getChapterNumber() + " – " + series.getTitle());

            realtimePushService.pushToUser(
                    series.getCreator().getEmail(),
                    RealtimeQueues.CHAPTER_UPDATES,
                    toResWithSeries(chapter)
            );
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

        Chapter chapter = chapterRepository.findByIdForUpdate(deadline.getChapter().getChapterId())
        .orElseThrow(() -> new CustomAppException("Không tìm thấy chapter", HttpStatus.NOT_FOUND));
        
        List<ChapterPageDeadline> chapterDeadlines = deadlineRepository
                .findByChapterChapterIdOrderByPageFrom(chapter.getChapterId());
        boolean allReadyForReview = !chapterDeadlines.isEmpty()
                && chapterDeadlines.stream().allMatch(d ->
                        "submitted".equals(d.getStatus()) || "approved".equals(d.getStatus()));

        if (allReadyForReview) {
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

        if (series != null && series.getEditor() != null) {
            notificationService.send(series.getEditor().getUserId(), "REVIEW",
                    isResubmit ? "🔄 Mangaka đã nộp lại trang" : "📥 Mangaka vừa nộp trang",
                    (isResubmit ? "Mangaka đã nộp lại" : "Mangaka vừa nộp")
                            + " nhóm trang " + deadline.getPageFrom() + "–" + deadline.getPageTo()
                            + " | Ch." + chapter.getChapterNumber()
                            + " – " + series.getTitle() + ". Hãy xem xét!");

            realtimePushService.pushToUser(
                    series.getEditor().getEmail(),
                    RealtimeQueues.PAGE_DEADLINE_UPDATES,
                    toDeadlineRes(deadline)
            );
        }

        ChapterRes freshChapter = toResWithSeries(chapter);
        if (series != null && series.getEditor() != null) {
            realtimePushService.pushToUser(series.getEditor().getEmail(), RealtimeQueues.CHAPTER_UPDATES, freshChapter);
        }
        if (series != null && series.getCreator() != null) {
            realtimePushService.pushToUser(series.getCreator().getEmail(), RealtimeQueues.CHAPTER_UPDATES, freshChapter);
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
              String mangakaEmail = chapter.getSeries().getCreator().getEmail();
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

              realtimePushService.pushToUser(
                    mangakaEmail,
                    RealtimeQueues.PAGE_DEADLINE_UPDATES,
                    toDeadlineRes(deadline)
              );
        }

        ChapterRes freshChapter = toResWithSeries(chapter);

        if (series != null && series.getEditor() != null) {
            realtimePushService.pushToUser(series.getEditor().getEmail(), RealtimeQueues.CHAPTER_UPDATES, freshChapter);
        }
        if (series != null && series.getCreator() != null) {
            realtimePushService.pushToUser(series.getCreator().getEmail(), RealtimeQueues.CHAPTER_UPDATES, freshChapter);
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

//        String currentStatus = chapter.getChapterStatus() != null
//                ? chapter.getChapterStatus().getChapterStatusName() : "";
//
//        boolean isFirstSubmit = "pages_submitted".equals(currentStatus);
//        boolean isResubmitAfterAdminRevision = "pending_publish".equals(currentStatus) && chapter.getAdminNote() != null;
//
//        if (!isFirstSubmit && !isResubmitAfterAdminRevision) {
//            throw new CustomAppException(
//                    "Chapter phải ở trạng thái 'pages_submitted' hoặc đã bị Admin yêu cầu sửa để submit lại",
//                    HttpStatus.BAD_REQUEST);
//        }


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
        chapter.setAdminNote(null);
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

        List<User> admins = userRepository.findAllByRoleName("ADMIN");
        ChapterRes payload = toResWithSeries(chapter);

        for (User admin : admins) {
            notificationService.send(admin.getUserId(), "REVIEW",
                    "📬 Chapter chờ duyệt đăng",
                    "Ch." + chapter.getChapterNumber()
                            + (chapter.getTitle() != null ? " – " + chapter.getTitle() : "")
                            + " | " + (series != null ? series.getTitle() : "")
                            + " (Tantou: " + tantou.getFullName() + ") đang chờ bạn duyệt đăng.");

            realtimePushService.pushToUser(admin.getEmail(), RealtimeQueues.ADMIN_CHAPTER_UPDATES, payload);
        }

        return payload;
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
        long completed = deadlines.stream()
                .filter(d -> "submitted".equals(d.getStatus()) || "approved".equals(d.getStatus()))
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
                .completedDeadlines(completed)
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
