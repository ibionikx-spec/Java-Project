package com.mangakousei.mangakousei_backend.entity.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mangakousei.mangakousei_backend.entity.status.ChapterStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chapters")
@Getter @Setter @NoArgsConstructor 
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@Builder
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id")
    @EqualsAndHashCode.Include
    private Long chapterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false)
    @JsonBackReference("ChapterSeries")
    private Series series;

    @Column(name = "chapter_number", nullable = false)
    private int chapterNumber;

    @Column(name = "title")
    private String title;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_status_id", nullable = false)
    private ChapterStatus chapterStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("ChapterPages")
    @Builder.Default
    private List<Page> pages = new ArrayList<>();
   
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
