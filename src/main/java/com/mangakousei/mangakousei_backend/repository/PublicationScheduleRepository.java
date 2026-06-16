package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.PublicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublicationScheduleRepository extends JpaRepository<PublicationSchedule, Long> {
    Optional<PublicationSchedule> findBySeriesSeriesId(Long seriesId);
    List<PublicationSchedule> findByScheduleType(String scheduleType);
}
