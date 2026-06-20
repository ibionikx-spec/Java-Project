package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findBySeriesSeriesIdOrderByChapterNumberAsc(Long seriesId);
    Optional<Chapter> findBySeriesSeriesIdAndChapterNumber(Long seriesId, int chapterNumber);

    List<Chapter> findBySeriesSeriesIdAndSeriesApprovedAtIsNotNullOrderByChapterNumberDesc(Long seriesId);
}
