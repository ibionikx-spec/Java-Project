package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.EditorAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EditorAnnotationRepository extends JpaRepository<EditorAnnotation, Long> {
    List<EditorAnnotation> findByPagePageIdOrderByCreatedAtDesc(Long pageId);
}
