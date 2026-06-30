package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.TantouMangakaAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TantouMangakaAssignmentRepository 
    extends JpaRepository<TantouMangakaAssignment, Long> {

    Optional<TantouMangakaAssignment> findByTantou_UserIdAndMangaka_UserIdAndIsActiveTrue
        (Long tantouId, Long mangakaId);

    List<TantouMangakaAssignment> findByMangaka_UserIdAndIsActiveTrue(Long mangakaId);

    List<TantouMangakaAssignment> findByTantou_UserIdAndIsActiveTrue(Long tantouId);

    @Modifying
    @Transactional
    @Query("UPDATE TantouMangakaAssignment t SET t.isActive = false WHERE t.tantou.userId = :tantouId AND t.mangaka.userId = :mangakaId")
    void deactivateAllFor(Long tantouId, Long mangakaId);

    Optional<TantouMangakaAssignment> findByTantouUserIdAndMangakaUserId(
            Long tantouId, Long mangakaId);
}
