package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.CreateTaskAttachmentReq;
import com.mangakousei.mangakousei_backend.dto.response.TaskAttachmentRes;
import com.mangakousei.mangakousei_backend.entity.entity.Series;
import com.mangakousei.mangakousei_backend.entity.entity.Task;
import com.mangakousei.mangakousei_backend.entity.entity.TaskAttachment;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.TaskAttachmentRepository;
import com.mangakousei.mangakousei_backend.repository.TaskRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskAttachmentService {

    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<TaskAttachmentRes> getForAssistant(Long taskId, Long assistantId) {
        Task task = getTask(taskId);
        assertAssistantOwnsTask(task, assistantId);
        return attachmentRepository.findByTaskTaskIdOrderByCreatedAtDesc(taskId)
                .stream().map(this::toRes).toList();
    }

    @Transactional(readOnly = true)
    public List<TaskAttachmentRes> getForMangaka(Long taskId, Long mangakaId) {
        Task task = getTask(taskId);
        assertMangakaOwnsTask(task, mangakaId);
        return attachmentRepository.findByTaskTaskIdOrderByCreatedAtDesc(taskId)
                .stream().map(this::toRes).toList();
    }

    @Transactional
    public TaskAttachmentRes create(Long taskId, CreateTaskAttachmentReq req, Long mangakaId) {
        Task task = getTask(taskId);
        assertMangakaOwnsTask(task, mangakaId);
        User uploader = userRepository.findById(mangakaId)
                .orElseThrow(() -> new CustomAppException("User not found", HttpStatus.NOT_FOUND));

        TaskAttachment attachment = TaskAttachment.builder()
                .task(task)
                .fileUrl(req.getFileUrl())
                .fileName(req.getFileName())
                .fileType(req.getFileType())
                .uploadedBy(uploader)
                .build();

        TaskAttachment saved = attachmentRepository.save(attachment);
        if (task.getAssignedTo() != null) {
            notificationService.send(task.getAssignedTo().getUserId(), "SYSTEM",
                    "Task co tai nguyen moi",
                    task.getTaskType().getTaskTypeName() + " vua duoc them file ho tro: " + req.getFileName());
        }
        return toRes(saved);
    }

    @Transactional
    public void delete(Long taskId, Long attachmentId, Long mangakaId) {
        Task task = getTask(taskId);
        assertMangakaOwnsTask(task, mangakaId);
        TaskAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new CustomAppException("Khong tim thay attachment", HttpStatus.NOT_FOUND));
        if (!attachment.getTask().getTaskId().equals(taskId)) {
            throw new CustomAppException("Attachment khong thuoc task", HttpStatus.BAD_REQUEST);
        }
        attachmentRepository.delete(attachment);
    }

    public TaskAttachmentRes toRes(TaskAttachment attachment) {
        return TaskAttachmentRes.builder()
                .attachmentId(attachment.getAttachmentId())
                .taskId(attachment.getTask().getTaskId())
                .fileUrl(attachment.getFileUrl())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .uploadedById(attachment.getUploadedBy() != null ? attachment.getUploadedBy().getUserId() : null)
                .uploadedByName(attachment.getUploadedBy() != null ? attachment.getUploadedBy().getFullName() : null)
                .createdAt(attachment.getCreatedAt())
                .build();
    }

    private Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomAppException("Khong tim thay task", HttpStatus.NOT_FOUND));
    }

    private void assertMangakaOwnsTask(Task task, Long mangakaId) {
        Series series = task.getRegion().getPage().getChapter().getSeries();
        if (series.getCreator() == null || !series.getCreator().getUserId().equals(mangakaId)) {
            throw new CustomAppException("Khong co quyen", HttpStatus.FORBIDDEN);
        }
    }

    private void assertAssistantOwnsTask(Task task, Long assistantId) {
        if (task.getAssignedTo() == null || !task.getAssignedTo().getUserId().equals(assistantId)) {
            throw new CustomAppException("Khong co quyen", HttpStatus.FORBIDDEN);
        }
    }
}
