package com.mangakousei.mangakousei_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mangakousei.mangakousei_backend.entity.entity.Chapter;

@Repository
public interface TantouScheduleRepository extends JpaRepository<Chapter, Long> {
}