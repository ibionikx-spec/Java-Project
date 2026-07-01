package com.mangakousei.mangakousei_backend.entity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "conversations",
        indexes = {
                @Index(name = "idx_conv_participant_a", columnList = "participant_a_id"),
                @Index(name = "idx_conv_participant_b", columnList = "participant_b_id"),
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_conv_participants",
                        columnNames = {"participant_a_id", "participant_b_id"})
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    @EqualsAndHashCode.Include
    private Long conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_a_id", nullable = false)
    private User participantA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_b_id", nullable = false)
    private User participantB;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_message_preview", length = 255)
    private String lastMessagePreview;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastMessageAt = this.createdAt;
    }
}