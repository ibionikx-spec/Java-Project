package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByConversationConversationIdOrderByCreatedAtDesc(
            Long conversationId, Pageable pageable);

    long countByConversationConversationIdAndSenderUserIdNotAndIsReadFalse(
            Long conversationId, Long userId);

    @Modifying
    @Query("""
            UPDATE ChatMessage m SET m.isRead = true
            WHERE m.conversation.conversationId = :conversationId
              AND m.sender.userId != :userId
              AND m.isRead = false
            """)
    void markConversationReadForUser(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId);
}