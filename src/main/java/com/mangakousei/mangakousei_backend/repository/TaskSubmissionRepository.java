package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {
    List<TaskSubmission> findByTaskTaskIdOrderBySubmittedAtDesc(Long taskId);
    List<TaskSubmission> findBySubmittedByUserIdOrderBySubmittedAtDesc(Long userId);

    List<TaskSubmission> findByTaskAssignedByUserIdAndTaskSubmissionStatusTaskSubmissionStatusName(
            Long mangakaId, String statusName);
}