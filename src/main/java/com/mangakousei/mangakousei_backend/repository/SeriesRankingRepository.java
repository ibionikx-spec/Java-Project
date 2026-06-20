package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.engagement.SeriesRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeriesRankingRepository extends JpaRepository<SeriesRanking, Long> {
    List<SeriesRanking> findTop100ByOrderByCalculatedAtDescRankingPositionAsc();
}
