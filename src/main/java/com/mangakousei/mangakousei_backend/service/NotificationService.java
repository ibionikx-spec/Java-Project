package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.NotificationRes;
import com.mangakousei.mangakousei_backend.entity.entity.Notification;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.entity.system.NotificationType;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.NotificationRepository;
import com.mangakousei.mangakousei_backend.repository.NotificationTypeRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final UserRepository userRepository;

    @Async("logExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(Long recipientId, String typeName, String title, String message) {
        try {
            User user = userRepository.findById(recipientId).orElse(null);
            if (user == null) return;

            NotificationType type = notificationTypeRepository
                    .findByNotificationTypeName(typeName)
                    .orElseGet(() -> notificationTypeRepository
                            .findByNotificationTypeName("SYSTEM")
                            .orElseThrow());

            Notification notif = Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .notificationType(type)
                    .isRead(false)
                    .build();

            notificationRepository.save(notif);
        } catch (Exception e) {
            log.warn("[Notification] Gửi thông báo thất bại (userId={}): {}", recipientId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationRes> getMyNotifications() {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationRepository
                .findTop20ByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.countByUserUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationRepository.markAllReadByUserId(userId);
    }

    @Transactional
    public void markOneRead(Long notificationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy thông báo", HttpStatus.NOT_FOUND));
        if (!notif.getUser().getUserId().equals(userId)) {
            throw new CustomAppException("Không có quyền", HttpStatus.FORBIDDEN);
        }
        notificationRepository.markOneReadByIdAndUserId(notificationId, userId);
    }

    private NotificationRes toRes(Notification n) {
        return NotificationRes.builder()
                .notificationId(n.getNotificationId())
                .title(n.getTitle())
                .message(n.getMessage())
                .notificationType(n.getNotificationType().getNotificationTypeName())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}