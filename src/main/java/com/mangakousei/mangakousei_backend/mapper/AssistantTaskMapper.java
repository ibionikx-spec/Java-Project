package com.mangakousei.mangakousei_backend.mapper;

import com.mangakousei.mangakousei_backend.dto.response.AssistantTaskRes;
import com.mangakousei.mangakousei_backend.entity.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssistantTaskMapper {
    public AssistantTaskRes toAssistantTaskRes(Task t) {
        PageRegion region = t.getRegion();
        Page page = region.getPage();
        Chapter chapter = page.getChapter();
        Series series = chapter.getSeries();

        List<TaskSubmission> submissions = t.getTaskSubmissions();
        int submissionCount = submissions != null ? submissions.size() : 0;
        String latestStatus = null;
        if (submissions != null && !submissions.isEmpty()) {
            latestStatus = submissions.stream()
                    .reduce((a, b) -> a.getSubmittedAt().isAfter(b.getSubmittedAt()) ? a : b)
                    .map(s -> s.getTaskSubmissionStatus().getTaskSubmissionStatusName())
                    .orElse(null);
        }

        return AssistantTaskRes.builder()
                .taskId(t.getTaskId())
                .taskTypeName(t.getTaskType() != null ? t.getTaskType().getTaskTypeName() : null)
                .description(t.getDescription())
                .deadline(t.getDeadline())
                .taskStatus(t.getTaskStatus() != null ? t.getTaskStatus().getTaskStatusName() : null)
                .assignedByName(t.getAssignedBy() != null ? t.getAssignedBy().getFullName() : null)
                .assignedByAvatarUrl(t.getAssignedBy() != null ? t.getAssignedBy().getAvatarUrl() : null)
                .regionId(region.getRegionId())
                .regionX(region.getX())
                .regionY(region.getY())
                .regionWidth(region.getWidth())
                .regionHeight(region.getHeight())
                .regionTypeName(region.getRegionType() != null ? region.getRegionType().getRegionTypeName() : null)
                .regionNote(region.getNote())
                .pageId(page.getPageId())
                .pageNumber(page.getPageNumber())
                .pageFileUrl(page.getFileUrl())
                .chapterId(chapter.getChapterId())
                .chapterNumber(chapter.getChapterNumber())
                .chapterTitle(chapter.getTitle())
                .seriesId(series.getSeriesId())
                .seriesTitle(series.getTitle())
                .submissionCount(submissionCount)
                .latestSubmissionStatus(latestStatus)
                .createdAt(t.getCreatedAt())
                .build();
    }
}