package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.status.SeriesStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeriesStatusRepository extends JpaRepository<SeriesStatus, Long> {
    Optional<SeriesStatus> findBySeriesStatusName(String seriesStatusName);
}
