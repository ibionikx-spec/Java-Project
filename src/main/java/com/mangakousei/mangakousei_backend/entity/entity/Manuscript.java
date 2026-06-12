package com.mangakousei.mangakousei_backend.entity.entity;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mangakousei.mangakousei_backend.entity.status.ManuscriptStatus;

import com.mangakousei.mangakousei_backend.entity.type.ManuscriptType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "manuscript")
@Getter @Setter @NoArgsConstructor 
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@Builder
public class Manuscript {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manuscript_id")
    @EqualsAndHashCode.Include
    private Long manuscriptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id" ,nullable = false)
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_id")
    @JsonBackReference("SubmittedManuscript")
    private User submitter;

    @Column(name = "url_field")
    private String urlField;

    @Column(name = "version_no")
    private int versionNo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manuscript_status_id", nullable = false)
    private ManuscriptStatus manuscriptStatus;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manuscript_type_id")
    private ManuscriptType manuscriptType;

    @PrePersist
    protected void onSubmitted(){
        this.submittedAt = LocalDateTime.now();
    }
}
