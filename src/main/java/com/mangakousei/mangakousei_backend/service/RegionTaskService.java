package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.CreateRegionReq;
import com.mangakousei.mangakousei_backend.dto.request.CreateTaskReq;
import com.mangakousei.mangakousei_backend.dto.response.RegionRes;
import com.mangakousei.mangakousei_backend.dto.response.TaskRes;
import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.entity.status.TaskStatus;
import com.mangakousei.mangakousei_backend.entity.type.RegionType;
import com.mangakousei.mangakousei_backend.entity.type.TaskType;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionTaskService {

    private final PageRegionRepository regionRepository;
    private final RegionTypeRepository regionTypeRepository;
    private final PageRepository pageRepository;
    private final TaskRepository taskRepository;
    private final TaskTypeRepository taskTypeRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;

    public List<RegionRes> getRegionsByPage(Long pageId) {
        return regionRepository.findByPagePageId(pageId)
                .stream()
                .map(this::toRegionRes)
                .collect(Collectors.toList());
    }

    @Transactional
    public RegionRes createRegion(CreateRegionReq req) {
        Page page = pageRepository.findById(req.getPageId())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy page", HttpStatus.NOT_FOUND));

        RegionType type = regionTypeRepository.findById(req.getRegionTypeId())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy region type", HttpStatus.BAD_REQUEST));

        PageRegion region = PageRegion.builder()
                .page(page)
                .x(req.getX())
                .y(req.getY())
                .width(req.getWidth())
                .height(req.getHeight())
                .regionType(type)
                .note(req.getNote())
                .build();

        return toRegionRes(regionRepository.save(region));
    }

    @Transactional
    public RegionRes updateRegion(Long regionId, CreateRegionReq req) {
        PageRegion region = regionRepository.findById(regionId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy region", HttpStatus.NOT_FOUND));

        region.setX(req.getX());
        region.setY(req.getY());
        region.setWidth(req.getWidth());
        region.setHeight(req.getHeight());
        region.setNote(req.getNote());

        if (req.getRegionTypeId() != null) {
            RegionType type = regionTypeRepository.findById(req.getRegionTypeId())
                    .orElseThrow(() -> new CustomAppException(
                            "Không tìm thấy region type", HttpStatus.BAD_REQUEST));
            region.setRegionType(type);
        }

        return toRegionRes(regionRepository.save(region));
    }

    @Transactional
    public void deleteRegion(Long regionId) {
        PageRegion region = regionRepository.findById(regionId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy region", HttpStatus.NOT_FOUND));
        regionRepository.delete(region);
    }

    @Transactional
    public TaskRes createTask(CreateTaskReq req) {
        User mangaka = getCurrentUser();

        PageRegion region = regionRepository.findById(req.getRegionId())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy region", HttpStatus.NOT_FOUND));

        TaskType type = taskTypeRepository.findById(req.getTaskTypeId())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy task type", HttpStatus.BAD_REQUEST));

        User assistant = userRepository.findById(req.getAssignedTo())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy assistant", HttpStatus.BAD_REQUEST));

        TaskStatus todoStatus = taskStatusRepository.findByTaskStatusName("todo")
                .or(() -> taskStatusRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy TaskStatus", HttpStatus.INTERNAL_SERVER_ERROR));

        Task task = Task.builder()
                .region(region)
                .assignedBy(mangaka)
                .assignedTo(assistant)
                .taskType(type)
                .deadline(req.getDeadline())
                .description(req.getDescription())
                .rate(req.getRate())
                .taskStatus(todoStatus)
                .build();

        return toTaskRes(taskRepository.save(task));
    }

    @Transactional
    public TaskRes updateTask(Long taskId, CreateTaskReq req) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy task", HttpStatus.NOT_FOUND));

        if (req.getRate() != null) task.setRate(req.getRate());

        if (req.getTaskTypeId() != null) {
            TaskType type = taskTypeRepository.findById(req.getTaskTypeId())
                    .orElseThrow(() -> new CustomAppException(
                            "Không tìm thấy task type", HttpStatus.BAD_REQUEST));
            task.setTaskType(type);
        }
        if (req.getAssignedTo() != null) {
            User assistant = userRepository.findById(req.getAssignedTo())
                    .orElseThrow(() -> new CustomAppException(
                            "Không tìm thấy assistant", HttpStatus.BAD_REQUEST));
            task.setAssignedTo(assistant);
        }
        if (req.getDeadline() != null) task.setDeadline(req.getDeadline());
        task.setDescription(req.getDescription());

        return toTaskRes(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy task", HttpStatus.NOT_FOUND));
        taskRepository.delete(task);
    }

    private RegionRes toRegionRes(PageRegion r) {
        List<TaskRes> tasks = r.getTasks() != null
                ? r.getTasks().stream().map(this::toTaskRes).collect(Collectors.toList())
                : List.of();

        return RegionRes.builder()
                .regionId(r.getRegionId())
                .x(r.getX())
                .y(r.getY())
                .width(r.getWidth())
                .height(r.getHeight())
                .regionTypeName(r.getRegionType() != null ? r.getRegionType().getRegionTypeName() : null)
                .regionTypeId(r.getRegionType() != null ? r.getRegionType().getRegionTypeId() : null)
                .note(r.getNote())
                .tasks(tasks)
                .build();
    }

    private TaskRes toTaskRes(Task t) {
        return TaskRes.builder()
                .taskId(t.getTaskId())
                .taskTypeName(t.getTaskType() != null ? t.getTaskType().getTaskTypeName() : null)
                .description(t.getDescription())
                .deadline(t.getDeadline())
                .taskStatus(t.getTaskStatus() != null ? t.getTaskStatus().getTaskStatusName() : null)
                .assignedToId(t.getAssignedTo() != null ? t.getAssignedTo().getUserId() : null)
                .assignedToName(t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : null)
                .assignedToAvatarUrl(t.getAssignedTo() != null ? t.getAssignedTo().getAvatarUrl() : null)
                .createdAt(t.getCreatedAt())
                .build();
    }

    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new CustomAppException(
                        "User not found", HttpStatus.NOT_FOUND));
    }
}