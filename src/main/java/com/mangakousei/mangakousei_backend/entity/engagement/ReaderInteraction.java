package com.mangakousei.mangakousei_backend.entity.engagement;

import com.mangakousei.mangakousei_backend.entity.entity.Series;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reader_interactions", uniqueConstraints =
        @UniqueConstraint(name = "uk_reader_interaction_series_visitor", columnNames = {"series_id", "visitor_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReaderInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interaction_id")
    private Long interactionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "series_id", nullable = false)
    private Series series;

    @Column(name = "visitor_id", nullable = false, length = 64)
    private String visitorId;

    @Column(name = "voted", nullable = false)
    @Builder.Default
    private boolean voted = false;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    void updateTimestamp() { updatedAt = LocalDateTime.now(); }
}
