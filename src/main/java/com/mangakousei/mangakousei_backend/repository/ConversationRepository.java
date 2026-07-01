package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByParticipantAUserIdAndParticipantBUserId(Long aId, Long bId);

    @Query("""
            SELECT c FROM Conversation c
            WHERE c.participantA.userId = :userId OR c.participantB.userId = :userId
            ORDER BY c.lastMessageAt DESC
            """)
    List<Conversation> findAllForUser(@Param("userId") Long userId);
}