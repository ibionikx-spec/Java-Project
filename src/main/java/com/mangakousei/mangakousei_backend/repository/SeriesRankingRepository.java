package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.engagement.SeriesRanking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeriesRankingRepository extends JpaRepository<SeriesRanking, Long> {
    List<SeriesRanking> findTop100ByOrderByCalculatedAtDescRankingPositionAsc();

    List<SeriesRanking> findByCalculatedAtOrderByRankingPositionAsc(LocalDateTime calculatedAt);

    Optional<SeriesRanking> findTopBySeriesSeriesIdOrderByCalculatedAtDesc(Long seriesId);

    Optional<SeriesRanking> findTopBySeriesSeriesIdAndCalculatedAtBeforeOrderByCalculatedAtDesc(
            Long seriesId,
            LocalDateTime calculatedAt
    );

    @Query("SELECT MAX(r.calculatedAt) FROM SeriesRanking r")
    Optional<LocalDateTime> findLatestCalculatedAt();

    @Query("SELECT COUNT(r) FROM SeriesRanking r WHERE r.calculatedAt = :calculatedAt")
    long countBySnapshotTime(@Param("calculatedAt") LocalDateTime calculatedAt);
}
