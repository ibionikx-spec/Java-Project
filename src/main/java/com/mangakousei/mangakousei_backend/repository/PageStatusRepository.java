package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.status.PageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PageStatusRepository extends JpaRepository<PageStatus, Long> {
    Optional<PageStatus> findByPageStatusName(String pageStatusName);
}