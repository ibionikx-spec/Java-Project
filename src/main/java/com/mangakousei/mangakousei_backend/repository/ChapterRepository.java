package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findBySeriesSeriesIdOrderByChapterNumberAsc(Long seriesId);

    Optional<Chapter> findBySeriesSeriesIdAndChapterNumber(Long seriesId, int chapterNumber);

    List<Chapter> findBySeriesSeriesIdAndSeriesApprovedAtIsNotNullOrderByChapterNumberDesc(Long seriesId);

    List<Chapter> findBySeriesEditorUserIdAndChapterStatusChapterStatusNameOrderByCreatedAtDesc(
            Long tantouId, String statusName);

    List<Chapter> findByChapterStatusChapterStatusNameOrderByCreatedAtDesc(String statusName);

    Optional<Chapter> findTopBySeriesSeriesIdOrderByChapterNumberDesc(Long seriesId);

    @Query("SELECT c FROM Chapter c WHERE c.series.editor.userId = :editorId")
    List<Chapter> findBySeriesEditorUserId(@Param("editorId") Long editorId);

    @Query("""
    SELECT COUNT(c) FROM Chapter c
    WHERE c.series.editor.userId = :editorId
      AND c.chapterStatus.chapterStatusName = :statusName
    """)

    long countBySeriesEditorAndStatus(
            @Param("editorId") Long editorId,
            @Param("statusName") String statusName);

    @Query("SELECT COUNT(s) FROM Series s WHERE s.editor.userId = :editorId")
    long countByEditorUserId(@Param("editorId") Long editorId);

    long countByChapterStatusChapterStatusName(String statusName);
}
