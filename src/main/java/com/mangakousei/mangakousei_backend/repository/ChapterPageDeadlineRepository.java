package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.ChapterPageDeadline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterPageDeadlineRepository extends JpaRepository<ChapterPageDeadline, Long> {
    List<ChapterPageDeadline> findByChapterChapterIdOrderByPageFrom(Long chapterId);
    boolean existsByChapterChapterIdAndStatus(Long chapterId, String status);
    long countByChapterChapterId(Long chapterId);
    long countByChapterChapterIdAndStatus(Long chapterId, String status);

    @Query("""
    SELECT d FROM ChapterPageDeadline d
    JOIN d.chapter c
    JOIN c.series s
    WHERE s.editor.userId = :tantouId
      AND d.status <> 'approved'
      AND d.dueDate <= :endDate
    ORDER BY d.dueDate ASC
    """)
    List<ChapterPageDeadline> findUpcomingByTantouId(
            @Param("tantouId") Long tantouId,
            @Param("endDate") java.time.LocalDate endDate);

    @Query("""
    SELECT COUNT(d) FROM ChapterPageDeadline d
    WHERE d.chapter.chapterId = :chapterId
      AND d.status <> 'approved'
      AND d.dueDate < :today
    """)
    long countOverdueByChapterChapterId(
            @Param("chapterId") Long chapterId,
            @Param("today") java.time.LocalDate today);

    @Query("""
    SELECT d FROM ChapterPageDeadline d
    JOIN d.chapter c
    JOIN c.series s
    WHERE s.creator.userId = :mangakaId
      AND d.status <> 'approved'
      AND d.dueDate <= :endDate
    ORDER BY d.dueDate ASC
    """)
    List<ChapterPageDeadline> findUpcomingByMangakaId(
            @Param("mangakaId") Long mangakaId,
            @Param("endDate") java.time.LocalDate endDate);

    @Query("""
    SELECT COUNT(d) FROM ChapterPageDeadline d
    JOIN d.chapter c
    JOIN c.series s
    WHERE s.creator.userId = :mangakaId
      AND d.status = :status
    """)
    long countByMangakaIdAndStatus(
            @Param("mangakaId") Long mangakaId,
            @Param("status") String status);


    @Query("""
    SELECT COUNT(d) FROM ChapterPageDeadline d
    JOIN d.chapter c
    JOIN c.series s
    WHERE s.creator.userId = :mangakaId
    """)
    long countByMangakaId(@Param("mangakaId") Long mangakaId);

    @Query("""
    SELECT COUNT(d) FROM ChapterPageDeadline d
    JOIN d.chapter c
    JOIN c.series s
    WHERE s.creator.userId = :mangakaId
      AND d.status IN :statuses
    """)
    long countByMangakaIdAndStatusIn(
            @Param("mangakaId") Long mangakaId,
            @Param("statuses") java.util.List<String> statuses);

    @Query("""
    SELECT d FROM ChapterPageDeadline d
    JOIN d.chapter c
    JOIN c.series s
    WHERE s.creator.userId = :mangakaId
      AND d.submittedAt IS NOT NULL
      AND d.submittedAt >= :since
    """)
    java.util.List<ChapterPageDeadline> findSubmittedByMangakaIdSince(
            @Param("mangakaId") Long mangakaId,
            @Param("since") java.time.LocalDateTime since);
}
