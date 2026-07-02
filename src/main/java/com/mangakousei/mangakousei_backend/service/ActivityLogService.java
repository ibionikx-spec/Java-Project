package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.LogContext;
import com.mangakousei.mangakousei_backend.dto.response.ActivityLogRes;
import com.mangakousei.mangakousei_backend.entity.entity.ActivityLog;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.entity.system.Role;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.ActivityLogRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository logRepository;
    private final UserRepository        userRepository;
    private static final List<String>   WIDGET_EXCLUDED_TYPES = List.of("LOGIN");

    @Async("logExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(LogContext ctx) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            User user   = userRepository.findById(userId).orElse(null);
            if (user == null) return;

            ActivityLog entry = ActivityLog.builder()
                    .user(user)
                    .actionType(ctx.getActionType().name())
                    .category(ctx.getActionType().getCategory())
                    .detail(ctx.getDetail())
                    .entityType(ctx.getEntityType())
                    .entityId(ctx.getEntityId())
                    .seriesId(ctx.getSeriesId())
                    .chapterId(ctx.getChapterId())
                    .build();

            logRepository.save(entry);

        } catch (Exception e) {
            log.warn("[ActivityLog] Ghi log thất bại: {}", e.getMessage());
        }
    }

    @Async("logExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, LogContext ctx) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;

            ActivityLog entry = ActivityLog.builder()
                    .user(user)
                    .actionType(ctx.getActionType().name())
                    .category(ctx.getActionType().getCategory())
                    .detail(ctx.getDetail())
                    .entityType(ctx.getEntityType())
                    .entityId(ctx.getEntityId())
                    .seriesId(ctx.getSeriesId())
                    .chapterId(ctx.getChapterId())
                    .build();

            logRepository.save(entry);

        } catch (Exception e) {
            log.warn("[ActivityLog] Ghi log thất bại (userId={}): {}", userId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogRes> getMyLogs(String category, int page, int size) {
        Long userId  = SecurityUtils.getCurrentUserId();
        var  pageable = PageRequest.of(page, size);

        Page<ActivityLog> logs = (category == null || category.isBlank())
                ? logRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable)
                : logRepository.findByUserUserIdAndCategoryOrderByCreatedAtDesc(userId, category, pageable);

        return logs.map(this::toRes);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogRes> getRecentLogs() {
        Long userId = SecurityUtils.getCurrentUserId();
        return logRepository
                .findRecentExcludingTypes(userId, WIDGET_EXCLUDED_TYPES, PageRequest.of(0, 10))
                .stream().map(this::toRes).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogRes> getAllLogs(
            Long userId,
            String category,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {
        if (!SecurityUtils.isAdmin()) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        return logRepository
                .findAllFiltered(userId, category, from, to, PageRequest.of(page, size))
                .map(this::toRes);
    }

    private ActivityLogRes toRes(ActivityLog a) {
        String role = a.getUser().getRoles().stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse(null);

        return ActivityLogRes.builder()
                .logId(a.getLogId())
                .actionType(a.getActionType())
                .category(a.getCategory())
                .detail(a.getDetail())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .seriesId(a.getSeriesId())
                .chapterId(a.getChapterId())
                .userId(a.getUser().getUserId())
                .userFullName(a.getUser().getFullName())
                .userAvatarUrl(a.getUser().getAvatarUrl())
                .userRole(role)
                .createdAt(a.getCreatedAt())
                .build();
    }
}