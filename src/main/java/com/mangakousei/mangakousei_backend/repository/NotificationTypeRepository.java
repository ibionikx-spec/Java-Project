package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.system.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTypeRepository extends JpaRepository<NotificationType, Long> {
    Optional<NotificationType> findByNotificationTypeName(String name);
}