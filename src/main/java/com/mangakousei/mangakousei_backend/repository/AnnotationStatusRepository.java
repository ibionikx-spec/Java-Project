package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.status.AnnotationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnnotationStatusRepository extends JpaRepository<AnnotationStatus, Long> {
    Optional<AnnotationStatus> findByAnnotationStatusName(String annotationStatusName);
}
