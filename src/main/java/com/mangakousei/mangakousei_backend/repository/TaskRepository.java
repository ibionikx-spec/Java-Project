package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByRegionRegionId(Long regionId);
    List<Task> findByAssignedToUserId(Long assistantId);
    List<Task> findByAssignedToUserIdAndTaskStatusTaskStatusName(Long assistantId, String statusName);
    List<Task> findByRegionPageChapterChapterId(Long chapterId);
}