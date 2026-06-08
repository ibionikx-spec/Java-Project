package com.mangakousei.mangakousei_backend.entity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mangakousei.mangakousei_backend.entity.engagement.ReaderVoteBatches;
import com.mangakousei.mangakousei_backend.entity.status.UserStatus;
import com.mangakousei.mangakousei_backend.entity.system.Role;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "phone")
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_status_id")
    private UserStatus userStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("notifications")
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();
    
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("createdSeries")
    @Builder.Default
    private List<Series> createdSeries = new ArrayList<>();
    
    @OneToMany(mappedBy = "editor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("editedSeries")
    @Builder.Default
    private List<Series> editedSeries = new ArrayList<>();

    @OneToMany(mappedBy = "submitter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("SubmittedManuscript")
    @Builder.Default
    private List<Manuscript> manuscripts = new ArrayList<>();

    @OneToMany(mappedBy = "editor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("EditorAnnotationId")
    @Builder.Default
    private List<EditorAnnotation> editorAnnotations = new ArrayList<>();

    @OneToMany(mappedBy = "decider", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("publicationUserDecisions")
        @Builder.Default
    private List<PublicationDecision> madeDecisions = new ArrayList<>();
    
    @OneToMany(mappedBy = "importer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("importedVoteBatches")
        @Builder.Default
    private List<ReaderVoteBatches> importVoteBatches = new ArrayList<>();
    
    @OneToMany(mappedBy = "assignedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("UserTask")
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "submittedBy", cascade = CascadeType.ALL, orphanRemoval =  true)
    @JsonManagedReference("UserSubmissionTask")
    @Builder.Default
    private List<TaskSubmission> userTaskSubmissions = new ArrayList<>();

    @OneToMany(mappedBy = "reviewedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("UserReviewed")
    @Builder.Default
    private List<TaskSubmission> userRevieweds = new ArrayList<>();

    @OneToMany(mappedBy = "assistant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("AssistantPaymentId")
    @Builder.Default
    private List<Payment> paymentAssistants = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}