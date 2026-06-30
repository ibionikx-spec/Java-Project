package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.type.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TaskTypeRepository extends JpaRepository<TaskType, Long> {
    Optional<TaskType> findByTaskTypeName(String taskTypeName);
}