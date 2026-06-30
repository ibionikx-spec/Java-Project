package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.PublicationDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicationDecisionRepository extends JpaRepository<PublicationDecision, Long> {
    List<PublicationDecision> findBySeriesSeriesIdOrderByDecidedAtDesc(Long seriesId);
}
