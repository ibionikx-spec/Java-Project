package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.status.TaskSubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TaskSubmissionStatusRepository extends JpaRepository<TaskSubmissionStatus, Long> {
    Optional<TaskSubmissionStatus> findByTaskSubmissionStatusName(String name);
}
