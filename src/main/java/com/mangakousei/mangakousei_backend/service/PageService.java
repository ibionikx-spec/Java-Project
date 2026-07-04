package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.constant.RealtimeQueues;
import com.mangakousei.mangakousei_backend.dto.request.CreatePageReq;
import com.mangakousei.mangakousei_backend.dto.request.UpdatePageReq;
import com.mangakousei.mangakousei_backend.dto.response.PageRes;
import com.mangakousei.mangakousei_backend.entity.entity.Chapter;
import com.mangakousei.mangakousei_backend.entity.entity.ChapterPageDeadline;
import com.mangakousei.mangakousei_backend.entity.entity.Page;
import com.mangakousei.mangakousei_backend.entity.entity.Series;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.entity.status.PageStatus;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.ChapterPageDeadlineRepository;
import com.mangakousei.mangakousei_backend.repository.ChapterRepository;
import com.mangakousei.mangakousei_backend.repository.PageRepository;
import com.mangakousei.mangakousei_backend.repository.PageStatusRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final ChapterRepository chapterRepository;
    private final PageStatusRepository pageStatusRepository;
    private final ChapterPageDeadlineRepository deadlineRepository;
    private final RealtimePushService realtimePushService;
    private final UserRepository userRepository;

    private void notifyPageImageChanged(Page page) {
        Chapter chapter = page.getChapter();
        if (chapter == null) return;
        Series series = chapter.getSeries();
        if (series == null || series.getEditor() == null) return;

        List<ChapterPageDeadline> deadlines = deadlineRepository
                .findByChapterChapterIdOrderByPageFrom(chapter.getChapterId());

        boolean isPendingPublish = chapter.getChapterStatus() != null
                && "pending_publish".equals(chapter.getChapterStatus().getChapterStatusName());

        for (ChapterPageDeadline d : deadlines) {
            if (page.getPageNumber() >= d.getPageFrom() && page.getPageNumber() <= d.getPageTo()) {
                realtimePushService.pushToUser(
                        series.getEditor().getEmail(),
                        RealtimeQueues.DEADLINE_PAGES_CHANGED,
                        d.getDeadlineId()
                );

                if (isPendingPublish) {
                    for (User admin : userRepository.findAllByRoleName("ADMIN")) {
                        realtimePushService.pushToUser(
                                admin.getEmail(),
                                RealtimeQueues.DEADLINE_PAGES_CHANGED,
                                d.getDeadlineId()
                        );
                    }
                }
            }
        }
    }

    public List<PageRes> getPagesByChapter(Long chapterId) {
        return pageRepository
                .findByChapterChapterIdOrderByPageNumberAsc(chapterId)
                .stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    @Transactional
    public PageRes createPage(CreatePageReq req) {
        Chapter chapter = chapterRepository.findById(req.getChapterId())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy chapter", HttpStatus.NOT_FOUND));

        pageRepository.findByChapterChapterIdAndPageNumber(
                        req.getChapterId(), req.getPageNumber())
                .ifPresent(p -> { throw new CustomAppException(
                        "Trang " + req.getPageNumber() + " đã tồn tại",
                        HttpStatus.CONFLICT); });

        PageStatus draftStatus = pageStatusRepository
                .findByPageStatusName("draft")
                .or(() -> pageStatusRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy PageStatus", HttpStatus.INTERNAL_SERVER_ERROR));

        Page page = Page.builder()
                .chapter(chapter)
                .pageNumber(req.getPageNumber())
                .fileUrl(req.getFileUrl())
                .status(draftStatus)
                .build();
        Page saved = pageRepository.save(page);
        notifyPageImageChanged(saved);

        return toRes(saved);
    }

    @Transactional
    public PageRes updatePage(Long pageId, UpdatePageReq req) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy page", HttpStatus.NOT_FOUND));

        if (req.getFileUrl() != null && !req.getFileUrl().isBlank()) {
            page.setFileUrl(req.getFileUrl());
        }
        if (req.getPageNumber() != null) {
            page.setPageNumber(req.getPageNumber());
        }

        Page saved = pageRepository.save(page);
        notifyPageImageChanged(saved);
        return toRes(saved);
    }

    @Transactional
    public void deletePage(Long pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy page", HttpStatus.NOT_FOUND));
        notifyPageImageChanged(page);
        pageRepository.delete(page);
    }

    private PageRes toRes(Page p) {
        int regionCount = p.getPageRegions() != null ? p.getPageRegions().size() : 0;
        int taskCount = p.getPageRegions() != null
                ? p.getPageRegions().stream()
                  .mapToInt(r -> r.getTasks() != null ? r.getTasks().size() : 0)
                  .sum()
                : 0;

        return PageRes.builder()
                .pageId(p.getPageId())
                .pageNumber(p.getPageNumber())
                .fileUrl(p.getFileUrl())
                .pageStatus(p.getStatus() != null ? p.getStatus().getPageStatusName() : null)
                .regionCount(regionCount)
                .taskCount(taskCount)
                .createdAt(p.getCreatedAt())
                .build();
    }
}