package com.mangakousei.mangakousei_backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.mangakousei.mangakousei_backend.entity.entity.Series;

import java.util.List;
import java.util.Optional;

public interface SeriesRepository extends JpaRepository<Series, Long> {
    long countByCreatorUserId(Long userId);
    long countByEditorUserId(Long userId);
    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.series.creator.userId = :userId")
    long countChaptersByCreatorUserId(@Param("userId") Long userId);
    @Query("SELECT COUNT(p) FROM Page p WHERE p.chapter.series.creator.userId = :userId")
    long countPagesByCreatorUserId(@Param("userId") Long userId);

    List<Series> findByCreatorUserIdOrderByApprovedAtDesc(Long userId);
    List<Series> findByEditorUserIdOrderByApprovedAtDesc(Long editorId);

    @Query("SELECT DISTINCT s FROM Series s LEFT JOIN FETCH s.genres LEFT JOIN FETCH s.creator WHERE s.approvedAt IS NOT NULL ORDER BY s.approvedAt DESC")
    List<Series> findAllPublicSeries();

    @Query("SELECT DISTINCT s FROM Series s LEFT JOIN FETCH s.genres LEFT JOIN FETCH s.creator WHERE s.seriesId = :seriesId AND s.approvedAt IS NOT NULL")
    Optional<Series> findPublicSeriesById(@Param("seriesId") Long seriesId);
}
