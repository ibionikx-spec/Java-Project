package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.engagement.ReaderInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ReaderInteractionRepository extends JpaRepository<ReaderInteraction, Long> {
    Optional<ReaderInteraction> findBySeriesSeriesIdAndVisitorId(Long seriesId, String visitorId);
    long countBySeriesSeriesIdAndVotedTrue(Long seriesId);

    @Query("SELECT COALESCE(AVG(i.rating), 0) FROM ReaderInteraction i WHERE i.series.seriesId = :seriesId AND i.rating IS NOT NULL")
    double averageRatingBySeriesId(@Param("seriesId") Long seriesId);

    long countBySeriesSeriesIdAndRatingIsNotNull(Long seriesId);
}
