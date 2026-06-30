package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.LogContext;
import com.mangakousei.mangakousei_backend.dto.request.ReviewSubmissionReq;
import com.mangakousei.mangakousei_backend.dto.request.SubmitTaskReq;
import com.mangakousei.mangakousei_backend.dto.response.AssistantTaskRes;
import com.mangakousei.mangakousei_backend.dto.response.TaskSubmissionRes;
import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.entity.status.TaskStatus;
import com.mangakousei.mangakousei_backend.entity.status.TaskSubmissionStatus;
import com.mangakousei.mangakousei_backend.entity.type.ActionType;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskSubmissionService {

    private final TaskSubmissionRepository submissionRepository;
    private final TaskSubmissionStatusRepository submissionStatusRepository;
    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    public List<AssistantTaskRes> getMyTasks(Long assistantId, String statusFilter) {
        List<Task> tasks;
        if (statusFilter != null && !statusFilter.isBlank()) {
            tasks = taskRepository.findByAssignedToUserIdAndTaskStatusTaskStatusName(
                    assistantId, statusFilter);
        } else {
            tasks = taskRepository.findByAssignedToUserId(assistantId);
        }
        return tasks.stream().map(this::toAssistantTaskRes).collect(Collectors.toList());
    }

    @Transactional
    public TaskSubmissionRes submitWork(SubmitTaskReq req, Long assistantId) {
        User assistant = getUserById(assistantId);

        Task task = taskRepository.findById(req.getTaskId())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy task", HttpStatus.NOT_FOUND));

        if (!task.getAssignedTo().getUserId().equals(assistantId)) {
            throw new CustomAppException(
                    "Bạn không được giao task này", HttpStatus.FORBIDDEN);
        }

        boolean isResubmit = !submissionRepository
                .findByTaskTaskIdOrderBySubmittedAtDesc(req.getTaskId())
                .isEmpty();

        TaskSubmissionStatus pendingStatus = submissionStatusRepository
                .findByTaskSubmissionStatusName("pending")
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy TaskSubmissionStatus 'pending'",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        TaskSubmission submission = TaskSubmission.builder()
                .task(task)
                .submittedBy(assistant)
                .fileUrl(req.getFileUrl())
                .note(req.getNote())
                .taskSubmissionStatus(pendingStatus)
                .build();

        TaskSubmission saved = submissionRepository.save(submission);

        taskStatusRepository.findByTaskStatusName("review")
                .ifPresent(reviewStatus -> {
                    task.setTaskStatus(reviewStatus);
                    taskRepository.save(task);
                });

        PageRegion region = task.getRegion();
        Page page = region != null ? region.getPage() : null;
        Chapter chapter = page != null ? page.getChapter() : null;
        Series series = chapter != null ? chapter.getSeries() : null;

        activityLogService.log(LogContext.builder()
                .actionType(isResubmit ? ActionType.RESUBMIT_TASK : ActionType.SUBMIT_TASK)
                .detail((isResubmit ? "Nộp lại" : "Nộp") + " task "
                        + task.getTaskType().getTaskTypeName()
                        + (page != null ? " – Trang " + page.getPageNumber() : "")
                        + (chapter != null ? " Ch." + chapter.getChapterNumber() : "")
                        + (series != null ? " | " + series.getTitle() : ""))
                .entityType("TASK_SUBMISSION")
                .entityId(saved.getTaskSubmissionId())
                .seriesId(series != null ? series.getSeriesId() : null)
                .chapterId(chapter != null ? chapter.getChapterId() : null)
                .build());

        notificationService.send(task.getAssignedBy().getUserId(), "REVIEW",
              (isResubmit ? "🔄 Assistant nộp lại bài" : "📥 Assistant nộp bài mới"),
              task.getAssignedBy().getFullName() != null
                  ? "Assistant " + assistant.getFullName() + (isResubmit ? " đã nộp lại" : " đã nộp")
                    + " task " + task.getTaskType().getTaskTypeName()
                    + (page != null ? " – Trang " + page.getPageNumber() : "")
                    + (chapter != null ? " Ch." + chapter.getChapterNumber() : "")
                    + (series != null ? " | " + series.getTitle() : "")
                  : "Có submission mới cần review");

        return toSubmissionRes(saved);
    }

    public List<TaskSubmissionRes> getSubmissionsByTask(Long taskId) {
        return submissionRepository.findByTaskTaskIdOrderBySubmittedAtDesc(taskId)
                .stream()
                .map(this::toSubmissionRes)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateTaskStatus(Long taskId, String newStatus, Long assistantId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy task", HttpStatus.NOT_FOUND));

        if (!task.getAssignedTo().getUserId().equals(assistantId)) {
            throw new CustomAppException(
                    "Bạn không được giao task này", HttpStatus.FORBIDDEN);
        }

        TaskStatus status = taskStatusRepository.findByTaskStatusName(newStatus)
                .orElseThrow(() -> new CustomAppException(
                        "Task status không hợp lệ: " + newStatus,
                        HttpStatus.BAD_REQUEST));

        task.setTaskStatus(status);
        taskRepository.save(task);
    }

    public List<TaskSubmissionRes> getPendingReviews(Long mangakaId) {
        return submissionRepository
                .findByTaskAssignedByUserIdAndTaskSubmissionStatusTaskSubmissionStatusName(
                        mangakaId, "pending")
                .stream()
                .map(this::toSubmissionRes)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskSubmissionRes reviewSubmission(Long submissionId,
                                              ReviewSubmissionReq req,
                                              Long mangakaId) {
        User mangaka = getUserById(mangakaId);

        TaskSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy submission", HttpStatus.NOT_FOUND));

        if (!submission.getTask().getAssignedBy().getUserId().equals(mangakaId)) {
            throw new CustomAppException(
                    "Bạn không có quyền review submission này",
                    HttpStatus.FORBIDDEN);
        }

        if (!"pending".equals(submission.getTaskSubmissionStatus().getTaskSubmissionStatusName())) {
            throw new CustomAppException(
                    "Submission này đã được review rồi",
                    HttpStatus.BAD_REQUEST);
        }

        Task task = submission.getTask();
        boolean approved = "approved".equals(req.getDecision());

        PageRegion region = task.getRegion();
        Page page = region != null ? region.getPage() : null;
        Chapter chapter = page != null ? page.getChapter() : null;
        Series series = chapter != null ? chapter.getSeries() : null;

        switch (req.getDecision()) {
            case "approved" -> {
                TaskSubmissionStatus approvedStatus = submissionStatusRepository
                        .findByTaskSubmissionStatusName("approved")
                        .orElseThrow();
                submission.setTaskSubmissionStatus(approvedStatus);
                submission.setReviewedBy(mangaka);
                submission.setReviewedAt(LocalDateTime.now());

                taskStatusRepository.findByTaskStatusName("done")
                        .ifPresent(doneStatus -> {
                            task.setTaskStatus(doneStatus);
                            taskRepository.save(task);
                        });

                paymentService.createPaymentOnApprove(task, submission, mangakaId);

                notificationService.send(submission.getSubmittedBy().getUserId(), "REVIEW",
                  "✅ Bài nộp được duyệt",
                  "Mangaka đã duyệt task " + task.getTaskType().getTaskTypeName()
                  + (page != null ? " – Trang " + page.getPageNumber() : "")
                  + (chapter != null ? " Ch." + chapter.getChapterNumber() : "")
                  + (series != null ? " | " + series.getTitle() : ""));
            }
            case "rejected" -> {
                TaskSubmissionStatus rejectedStatus = submissionStatusRepository
                        .findByTaskSubmissionStatusName("rejected")
                        .orElseThrow();
                submission.setTaskSubmissionStatus(rejectedStatus);
                submission.setReviewedBy(mangaka);
                submission.setReviewedAt(LocalDateTime.now());

                if (req.getFeedback() != null && !req.getFeedback().isBlank()) {
                    submission.setNote(
                            (submission.getNote() != null ? submission.getNote() + "\n\n" : "")
                                    + "[Mangaka] " + req.getFeedback());
                }

                taskStatusRepository.findByTaskStatusName("doing")
                        .ifPresent(doingStatus -> {
                            task.setTaskStatus(doingStatus);
                            taskRepository.save(task);
                        });

                String feedback = req.getFeedback() != null && !req.getFeedback().isBlank()
                          ? ": " + req.getFeedback() : "";
                notificationService.send(submission.getSubmittedBy().getUserId(), "REVIEW",
                          "✏️ Bài nộp cần chỉnh sửa",
                          "Mangaka yêu cầu chỉnh sửa task " + task.getTaskType().getTaskTypeName()
                          + (page != null ? " – Trang " + page.getPageNumber() : "")
                          + (chapter != null ? " Ch." + chapter.getChapterNumber() : "")
                          + (series != null ? " | " + series.getTitle() : "") + feedback);
            }
            default -> throw new CustomAppException(
                    "Decision không hợp lệ: 'approved' hoặc 'rejected'",
                    HttpStatus.BAD_REQUEST);
        }

        TaskSubmissionRes result = toSubmissionRes(submissionRepository.save(submission));

        activityLogService.log(LogContext.builder()
                .actionType(approved ? ActionType.REVIEW_APPROVED : ActionType.REVIEW_REVISION)
                .detail((approved ? "Duyệt" : "Từ chối") + " submission task "
                        + task.getTaskType().getTaskTypeName()
                        + (page != null ? " – Trang " + page.getPageNumber() : "")
                        + (chapter != null ? " Ch." + chapter.getChapterNumber() : "")
                        + (series != null ? " | " + series.getTitle() : ""))
                .entityType("TASK_SUBMISSION")
                .entityId(submissionId)
                .seriesId(series != null ? series.getSeriesId() : null)
                .chapterId(chapter != null ? chapter.getChapterId() : null)
                .build());

        return result;
    }

    private TaskSubmissionRes toSubmissionRes(TaskSubmission s) {
        Task task = s.getTask();
        PageRegion region = task.getRegion();
        Page page = region.getPage();

        return TaskSubmissionRes.builder()
                .submissionId(s.getTaskSubmissionId())
                .fileUrl(s.getFileUrl())
                .note(s.getNote())
                .status(s.getTaskSubmissionStatus() != null
                        ? s.getTaskSubmissionStatus().getTaskSubmissionStatusName() : null)
                .submittedAt(s.getSubmittedAt())
                .reviewedAt(s.getReviewedAt())
                .submittedById(s.getSubmittedBy().getUserId())
                .submittedByName(s.getSubmittedBy().getFullName())
                .submittedByAvatarUrl(s.getSubmittedBy().getAvatarUrl())
                .reviewedById(s.getReviewedBy() != null ? s.getReviewedBy().getUserId() : null)
                .reviewedByName(s.getReviewedBy() != null ? s.getReviewedBy().getFullName() : null)
                .taskId(task.getTaskId())
                .taskTypeName(task.getTaskType() != null ? task.getTaskType().getTaskTypeName() : null)
                .taskDescription(task.getDescription())
                .pageId(page.getPageId())
                .pageNumber(page.getPageNumber())
                .build();
    }

    private AssistantTaskRes toAssistantTaskRes(Task t) {
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

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomAppException(
                        "User not found", HttpStatus.NOT_FOUND));
    }
}