package com.mangakousei.mangakousei_backend.entity.engagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mangakousei.mangakousei_backend.entity.entity.Series;

@Entity
@Table(name = "series_ranking")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class SeriesRanking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "series_ranking_id")
    private Long seriesRankingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false)
    @ToString.Exclude
    private Series series;

    @Column(name = "ranking_position", nullable = false)
    private Integer rankingPosition;

    @Column(name = "vote_count", nullable = false)
    private Long voteCount;

    @Column(name = "survey_score", precision = 10, scale = 2)
    private BigDecimal surveyScore;

    @Column(name = "sales_count", nullable = false)
    private Long salesCount;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
    
    @PrePersist
    protected void onCalculated() {
        this.calculatedAt = LocalDateTime.now();
    }
}
