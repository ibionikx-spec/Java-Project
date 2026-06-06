package com.mangakousei.mangakousei_backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "series")
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Series{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "series_id")
    @EqualsAndHashCode.Include
    private Long seriesId;

    @Column(name = "title", nullable = false, length = 255)
    private String tittle;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "series_genres",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    @ToString.Exclude
    private List<Genre> genres = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mangaka_id")
    @JsonBackReference("createdSeries")
    @ToString.Exclude
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tantou_editor_id")
    @JsonBackReference("editedSeries")
    @ToString.Exclude
    private User editor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    @ToString.Exclude
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_type_id", nullable = false)
    @ToString.Exclude
    private PublicationType publicationType;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("publicationDecisions")
    @Builder.Default
    @ToString.Exclude
    private List<PublicationDecision> decisions = new ArrayList<>();

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("seriesVoting")
    @Builder.Default
    @ToString.Exclude
    private List<ReaderVote> votes = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
    }
    protected void onApproved(){
        this.approvedAt = LocalDateTime.now();
    }
}