package com.mangakousei.mangakousei_backend.entity.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mangakousei.mangakousei_backend.entity.type.DecisionType;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "publication_decisions")
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "decisionId")
public class PublicationDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "decision_id")
    private Long decisionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_type_id", nullable = false)
    @ToString.Exclude
    private DecisionType decisionType;
    
    @Column(name = "reason",columnDefinition = "TEXT")
    private String reason;
    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false)
    @JsonBackReference("publicationSeriesDecisions")
    @ToString.Exclude
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("publicationUserDecisions")
    @JoinColumn(name = "decided_by")
    private User decider;

    @PrePersist
    protected void onDecided(){
        this.decidedAt = LocalDateTime.now();
    }
}