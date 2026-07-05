package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.engagement.ReaderVoteBatches;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReaderVoteBatchRepository extends JpaRepository<ReaderVoteBatches, Long> {
    List<ReaderVoteBatches> findTop20ByOrderByImportedAtDesc();
}
