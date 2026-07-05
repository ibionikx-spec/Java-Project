package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.CreateAnnotationReq;
import com.mangakousei.mangakousei_backend.dto.request.UpdateAnnotationReq;
import com.mangakousei.mangakousei_backend.dto.response.AnnotationRes;
import com.mangakousei.mangakousei_backend.entity.entity.EditorAnnotation;
import com.mangakousei.mangakousei_backend.entity.entity.Page;
import com.mangakousei.mangakousei_backend.entity.entity.Series;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.entity.status.AnnotationStatus;
import com.mangakousei.mangakousei_backend.entity.type.AnnotationType;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.AnnotationStatusRepository;
import com.mangakousei.mangakousei_backend.repository.AnnotationTypeRepository;
import com.mangakousei.mangakousei_backend.repository.EditorAnnotationRepository;
import com.mangakousei.mangakousei_backend.repository.PageRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EditorAnnotationService {

    private final EditorAnnotationRepository annotationRepository;
    private final AnnotationTypeRepository annotationTypeRepository;
    private final AnnotationStatusRepository annotationStatusRepository;
    private final PageRepository pageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<AnnotationRes> getForPageAsTantou(Long pageId, Long tantouId) {
        Page page = getPage(pageId);
        assertTantouOwnsPage(page, tantouId);
        return annotationRepository.findByPagePageIdOrderByCreatedAtDesc(pageId)
                .stream().map(this::toRes).toList();
    }

    @Transactional(readOnly = true)
    public List<AnnotationRes> getForPageAsMangaka(Long pageId, Long mangakaId) {
        Page page = getPage(pageId);
        assertMangakaOwnsPage(page, mangakaId);
        return annotationRepository.findByPagePageIdOrderByCreatedAtDesc(pageId)
                .stream().map(this::toRes).toList();
    }

    @Transactional
    public AnnotationRes create(CreateAnnotationReq req, Long tantouId) {
        Page page = getPage(req.getPageId());
        assertTantouOwnsPage(page, tantouId);

        User editor = userRepository.findById(tantouId)
                .orElseThrow(() -> new CustomAppException("User not found", HttpStatus.NOT_FOUND));
        AnnotationType type = annotationTypeRepository.findById(req.getAnnotationTypeId())
                .orElseThrow(() -> new CustomAppException("Khong tim thay annotation type", HttpStatus.BAD_REQUEST));
        AnnotationStatus open = getOrCreateStatus("open");

        EditorAnnotation annotation = new EditorAnnotation();
        annotation.setPage(page);
        annotation.setEditor(editor);
        annotation.setX(req.getX());
        annotation.setY(req.getY());
        annotation.setWidth(req.getWidth());
        annotation.setHeight(req.getHeight());
        annotation.setAnnotationType(type);
        annotation.setAnnotationStatus(open);
        annotation.setCommentText(req.getCommentText());

        EditorAnnotation saved = annotationRepository.save(annotation);
        notifyMangaka(page, "Tantou da danh dau trang can sua", req.getCommentText());
        return toRes(saved);
    }

    @Transactional
    public AnnotationRes update(Long annotationId, UpdateAnnotationReq req, Long tantouId) {
        EditorAnnotation annotation = getAnnotation(annotationId);
        assertTantouOwnsPage(annotation.getPage(), tantouId);

        if (req.getX() != null) annotation.setX(req.getX());
        if (req.getY() != null) annotation.setY(req.getY());
        if (req.getWidth() != null) annotation.setWidth(req.getWidth());
        if (req.getHeight() != null) annotation.setHeight(req.getHeight());
        if (req.getAnnotationTypeId() != null) {
            AnnotationType type = annotationTypeRepository.findById(req.getAnnotationTypeId())
                    .orElseThrow(() -> new CustomAppException("Khong tim thay annotation type", HttpStatus.BAD_REQUEST));
            annotation.setAnnotationType(type);
        }
        annotation.setCommentText(req.getCommentText());
        return toRes(annotationRepository.save(annotation));
    }

    @Transactional
    public AnnotationRes resolve(Long annotationId, Long userId) {
        EditorAnnotation annotation = getAnnotation(annotationId);
        Page page = annotation.getPage();
        Series series = page.getChapter().getSeries();
        boolean allowed = (series.getEditor() != null && series.getEditor().getUserId().equals(userId))
                || (series.getCreator() != null && series.getCreator().getUserId().equals(userId));
        if (!allowed) throw new CustomAppException("Khong co quyen", HttpStatus.FORBIDDEN);

        annotation.setAnnotationStatus(getOrCreateStatus("resolved"));
        return toRes(annotationRepository.save(annotation));
    }

    @Transactional
    public void delete(Long annotationId, Long tantouId) {
        EditorAnnotation annotation = getAnnotation(annotationId);
        assertTantouOwnsPage(annotation.getPage(), tantouId);
        annotationRepository.delete(annotation);
    }

    private EditorAnnotation getAnnotation(Long annotationId) {
        return annotationRepository.findById(annotationId)
                .orElseThrow(() -> new CustomAppException("Khong tim thay annotation", HttpStatus.NOT_FOUND));
    }

    private Page getPage(Long pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> new CustomAppException("Khong tim thay page", HttpStatus.NOT_FOUND));
    }

    private void assertTantouOwnsPage(Page page, Long tantouId) {
        Series series = page.getChapter().getSeries();
        if (series.getEditor() == null || !series.getEditor().getUserId().equals(tantouId)) {
            throw new CustomAppException("Khong co quyen", HttpStatus.FORBIDDEN);
        }
    }

    private void assertMangakaOwnsPage(Page page, Long mangakaId) {
        Series series = page.getChapter().getSeries();
        if (series.getCreator() == null || !series.getCreator().getUserId().equals(mangakaId)) {
            throw new CustomAppException("Khong co quyen", HttpStatus.FORBIDDEN);
        }
    }

    private AnnotationStatus getOrCreateStatus(String statusName) {
        return annotationStatusRepository.findByAnnotationStatusName(statusName)
                .orElseGet(() -> annotationStatusRepository.save(AnnotationStatus.builder()
                        .annotationStatusName(statusName)
                        .build()));
    }

    private void notifyMangaka(Page page, String title, String message) {
        Series series = page.getChapter().getSeries();
        if (series.getCreator() != null) {
            notificationService.send(series.getCreator().getUserId(), "SYSTEM", title,
                    series.getTitle() + " - Trang " + page.getPageNumber() + ": " + message);
        }
    }

    private AnnotationRes toRes(EditorAnnotation annotation) {
        return AnnotationRes.builder()
                .annotationId(annotation.getEditorAnnotationId())
                .pageId(annotation.getPage().getPageId())
                .x(annotation.getX())
                .y(annotation.getY())
                .width(annotation.getWidth())
                .height(annotation.getHeight())
                .commentText(annotation.getCommentText())
                .annotationTypeId(annotation.getAnnotationType() != null
                        ? annotation.getAnnotationType().getAnnotationTypeId() : null)
                .annotationTypeName(annotation.getAnnotationType() != null
                        ? annotation.getAnnotationType().getAnnotationTypeName() : null)
                .status(annotation.getAnnotationStatus() != null
                        ? annotation.getAnnotationStatus().getAnnotationStatusName() : null)
                .editorId(annotation.getEditor() != null ? annotation.getEditor().getUserId() : null)
                .editorName(annotation.getEditor() != null ? annotation.getEditor().getFullName() : null)
                .createdAt(annotation.getCreatedAt())
                .build();
    }
}
