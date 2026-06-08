package com.mangakousei.mangakousei_backend.entity.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mangakousei.mangakousei_backend.entity.status.PageStatus;

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
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "pages")
@Getter @Setter @NoArgsConstructor @Builder @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_id")
    @EqualsAndHashCode.Include
    private Long pageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    @JsonBackReference("ChapterPages")
    private Chapter chapter;

    @Column(name = "page_number", nullable = false)
    private int pageNumber;

    @Column(name = "fileUrl")
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_status_id", nullable = false)
    private PageStatus status;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("PageRegion")
    @Builder.Default
    private List<PageRegion> pageRegions = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreated(){
        this.createdAt = LocalDateTime.now();
    }
}
