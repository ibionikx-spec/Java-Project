package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.type.AnnotationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnnotationTypeRepository extends JpaRepository<AnnotationType, Long> {
    Optional<AnnotationType> findByAnnotationTypeName(String annotationTypeName);
}
