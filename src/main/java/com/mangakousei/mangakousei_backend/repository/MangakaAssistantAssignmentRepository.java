package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.MangakaAssistantAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MangakaAssistantAssignmentRepository
        extends JpaRepository<MangakaAssistantAssignment, Long> {

    List<MangakaAssistantAssignment> findByMangakaUserIdAndStatus(
            Long mangakaId, String status);

    List<MangakaAssistantAssignment> findByAssistantUserIdAndStatus(
            Long assistantId, String status);

    Optional<MangakaAssistantAssignment> findTopByMangakaUserIdAndAssistantUserIdOrderByInvitedAtDesc(
            Long mangakaId, Long assistantId);

    long countByAssistantUserIdAndStatus(Long assistantId, String status);

    List<MangakaAssistantAssignment> findByMangakaUserIdOrderByInvitedAtDesc(Long mangakaId);
}