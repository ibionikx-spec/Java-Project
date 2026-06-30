package com.mangakousei.mangakousei_backend.dto.request;

import com.mangakousei.mangakousei_backend.entity.type.ActionType;
import lombok.Builder;
import lombok.Getter;

/**
 * Object nhỏ gọn truyền metadata log vào ActivityLogService.
 *
 * Ví dụ dùng trong ChapterService:
 * <pre>{@code
 * logService.log(LogContext.builder()
 *     .actionType(ActionType.REVIEW_APPROVED)
 *     .detail("Duyệt nhóm trang Ch." + chapter.getChapterNumber() + " – " + series.getTitle())
 *     .entityType("PAGE_GROUP")
 *     .entityId(deadline.getDeadlineId())
 *     .seriesId(series.getSeriesId())
 *     .chapterId(chapter.getChapterId())
 *     .build());
 * }</pre>
 */
@Getter
@Builder
public class LogContext {

    private final ActionType actionType;

    private final String detail;

    private final String entityType;
    private final Long   entityId;

    private final Long seriesId;
    private final Long chapterId;
}