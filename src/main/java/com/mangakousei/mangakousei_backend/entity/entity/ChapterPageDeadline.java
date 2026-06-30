package com.mangakousei.mangakousei_backend.entity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chapter_page_deadlines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChapterPageDeadline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deadline_id")
    @EqualsAndHashCode.Include
    private Long deadlineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "page_from", nullable = false)
    private Integer pageFrom;

    @Column(name = "page_to", nullable = false)
    private Integer pageTo;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_by")
    private User setBy;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "pending";
    }
}