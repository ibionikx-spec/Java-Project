package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.status.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    Optional<TaskStatus> findByTaskStatusName(String taskStatusName);
}