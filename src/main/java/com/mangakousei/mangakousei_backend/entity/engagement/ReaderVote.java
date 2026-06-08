package com.mangakousei.mangakousei_backend.entity.engagement;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mangakousei.mangakousei_backend.entity.entity.Series;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.FetchType;
@Entity
@Table(name = "reader_vote")
@Getter @Setter @NoArgsConstructor
public class ReaderVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reader_vote_id")
    private Long readerVoteId;
   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    @JsonBackReference("ReaderVoteBatches")
    @ToString.Exclude
    private ReaderVoteBatches batch;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    @JsonBackReference("seriesVoting")
    @ToString.Exclude
    private Series series;
    
    @Column(name = "vote_count")
    private Long voteCount;
    
    @Column(name = "survey_score")
    private BigDecimal surveyScore;

    @Column(name = "sales_count")
    private Long salesCount;

    @Column(name = "comment_count")
    private Long commentCount;
}
