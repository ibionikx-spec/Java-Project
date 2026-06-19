package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.ChapterPageDeadline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterPageDeadlineRepository extends JpaRepository<ChapterPageDeadline, Long> {
    List<ChapterPageDeadline> findByChapterChapterIdOrderByPageFrom(Long chapterId);
    boolean existsByChapterChapterIdAndStatus(Long chapterId, String status);
    long countByChapterChapterId(Long chapterId);
    long countByChapterChapterIdAndStatus(Long chapterId, String status);
}
