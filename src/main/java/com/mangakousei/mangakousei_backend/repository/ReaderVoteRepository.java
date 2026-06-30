package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.engagement.ReaderVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReaderVoteRepository extends JpaRepository<ReaderVote, Long> {
    @Query("SELECT COALESCE(SUM(v.voteCount), 0) FROM ReaderVote v WHERE v.series.seriesId = :seriesId")
    long sumVotesBySeriesId(@Param("seriesId") Long seriesId);

    @Query("SELECT COALESCE(AVG(v.surveyScore), 0) FROM ReaderVote v WHERE v.series.seriesId = :seriesId AND v.surveyScore IS NOT NULL")
    double averageScoreBySeriesId(@Param("seriesId") Long seriesId);
}
