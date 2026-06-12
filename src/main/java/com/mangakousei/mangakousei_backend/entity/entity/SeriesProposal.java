package com.mangakousei.mangakousei_backend.entity.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "series_proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SeriesProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "proposal_id")
    @EqualsAndHashCode.Include
    private Long proposalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mangaka_id", nullable = false)
    @JsonBackReference("MangakaProposals")
    private User mangaka;

    @Column(name = "working_title", nullable = false)
    private String workingTitle;

    @Column(name = "synopsis", nullable = false, columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "target_audience", nullable = false, length = 50)
    private String targetAudience;

    @Column(name = "name_summary", columnDefinition = "TEXT")
    private String nameSummary;

    @Column(name = "sketch_image_url")
    private String sketchImageUrl;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    @JsonBackReference("EditorReviewedProposals")
    private User reviewedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("ProposalGenres")
    @Builder.Default
    private List<ProposalGenre> proposalGenres = new ArrayList<>();

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("ProposalCharacters")
    @Builder.Default
    private List<ProposalCharacter> proposalCharacters = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addGenre(Genre genre) {
        ProposalGenre proposalGenre = new ProposalGenre(this, genre);
        proposalGenres.add(proposalGenre);
    }

    public void removeGenre(Genre genre) {
        proposalGenres.removeIf(pg -> pg.getGenre().equals(genre));
    }

    public void addCharacter(ProposalCharacter character) {
        character.setProposal(this);
        proposalCharacters.add(character);
    }

    public void removeCharacter(ProposalCharacter character) {
        proposalCharacters.remove(character);
        character.setProposal(null);
    }
}