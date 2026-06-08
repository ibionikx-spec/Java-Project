package com.mangakousei.mangakousei_backend.entity.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mangakousei.mangakousei_backend.entity.status.AnnotationStatus;
import com.mangakousei.mangakousei_backend.entity.type.AnnotationType;

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

@Entity
@Table(name = "editor_annotation")
@Getter
@Setter
@NoArgsConstructor
public class EditorAnnotation{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "editor_annotation_id")
    @EqualsAndHashCode.Include
    private Long editorAnnotationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id", nullable = false)
    @JsonBackReference("EditorAnnotationId")
    private User editor;

    @Column(name = "x")
    private BigDecimal x;

    @Column(name = "y")
    private BigDecimal y;

    @Column(name = "height")
    private BigDecimal height;

    @Column(name = "width")
    private BigDecimal width;
    
    @Column(name = "comment_text", columnDefinition = "TEXT")
    private String commentText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotation_type_id", nullable = false)
    private AnnotationType annotationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotation_status_id", nullable = false)
    private AnnotationStatus annotationStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreated(){
        this.createdAt = LocalDateTime.now();
    }
}
