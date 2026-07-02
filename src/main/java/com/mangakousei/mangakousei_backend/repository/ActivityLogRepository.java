package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findByUserUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<ActivityLog> findByUserUserIdAndCategoryOrderByCreatedAtDesc(
            Long userId, String category, Pageable pageable);

    java.util.List<ActivityLog> findTop10ByUserUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
            SELECT a FROM ActivityLog a
            WHERE (:userId   IS NULL OR a.user.userId = :userId)
              AND (:category IS NULL OR a.category    = :category)
              AND (:from     IS NULL OR a.createdAt  >= :from)
              AND (:to       IS NULL OR a.createdAt  <= :to)
            ORDER BY a.createdAt DESC
            """)
    Page<ActivityLog> findAllFiltered(
            @Param("userId")   Long userId,
            @Param("category") String category,
            @Param("from")     java.time.LocalDateTime from,
            @Param("to")       java.time.LocalDateTime to,
            Pageable pageable
    );

    Page<ActivityLog> findBySeriesIdOrderByCreatedAtDesc(Long seriesId, Pageable pageable);

    Page<ActivityLog> findByChapterIdOrderByCreatedAtDesc(Long chapterId, Pageable pageable);

    @Query("""
        SELECT a FROM ActivityLog a
        WHERE a.user.userId = :userId
          AND a.actionType NOT IN :excludedTypes
        ORDER BY a.createdAt DESC
        """)
    List<ActivityLog> findRecentExcludingTypes(
            @Param("userId") Long userId,
            @Param("excludedTypes") java.util.List<String> excludedTypes,
            org.springframework.data.domain.Pageable pageable);
}