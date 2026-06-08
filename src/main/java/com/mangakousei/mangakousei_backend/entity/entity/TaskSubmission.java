package com.mangakousei.mangakousei_backend.entity.entity;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mangakousei.mangakousei_backend.entity.status.TaskSubmissionStatus;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_submission")
@Getter @Setter @NoArgsConstructor 
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@Builder
public class TaskSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_submission_id")
    @EqualsAndHashCode.Include
    private Long taskSubmissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("TaskSubmission")
    @JoinColumn(name = "task_id", nullable =  false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("UserSubmissionTask")
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("UserReviewed")
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "file_url", nullable =  false)
    private String fileUrl;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_submission_status_id", nullable = false)
    private TaskSubmissionStatus taskSubmissionStatus;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onSubmitted(){
        this.submittedAt = LocalDateTime.now();
    }
}
