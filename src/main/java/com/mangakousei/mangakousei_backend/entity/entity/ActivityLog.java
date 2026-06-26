package com.mangakousei.mangakousei_backend.entity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_log_user",      columnList = "user_id"),
        @Index(name = "idx_activity_log_category",  columnList = "category"),
        @Index(name = "idx_activity_log_created_at",columnList = "created_at"),
        @Index(name = "idx_activity_log_series",    columnList = "series_id"),
        @Index(name = "idx_activity_log_chapter",   columnList = "chapter_id"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "action_type", nullable = false, length = 80)
    private String actionType;

    @Column(name = "category", nullable = false, length = 40)
    private String category;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Column(name = "entity_type", length = 40)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "series_id")
    private Long seriesId;

    @Column(name = "chapter_id")
    private Long chapterId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}