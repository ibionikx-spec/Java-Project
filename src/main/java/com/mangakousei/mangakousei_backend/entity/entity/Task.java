package com.mangakousei.mangakousei_backend.entity.entity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mangakousei.mangakousei_backend.entity.status.TaskStatus;
import com.mangakousei.mangakousei_backend.entity.type.TaskType;

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
@Table(name = "task")
@Getter @Setter @NoArgsConstructor 
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@Builder
public class Task {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @EqualsAndHashCode.Include
   @Column(name = "task_id")
   private Long taskId;

   @ManyToOne(fetch = FetchType.LAZY)
   @JsonBackReference("RegionTask")
   @JoinColumn(name = "region_id", nullable = false)
   private PageRegion region;
   
   @ManyToOne(fetch = FetchType.LAZY)
   @JsonBackReference("UserTask")
   @JoinColumn(name = "assigned_by", nullable = false)
   private User assignedBy;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name ="task_type_id", nullable = false)
   private TaskType taskType;

   @Column(name = "description", columnDefinition = "TEXT")
   private String description;

   @Column(name = "deadline", nullable = false)
   private LocalDateTime deadline;

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "task_status_id", nullable = false)
   private TaskStatus taskStatus;

   @Column(name = "created_at")
   private LocalDateTime createdAt;

   @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
   @JsonManagedReference("TaskSubmission")
   @Builder.Default
   private List<TaskSubmission> taskSubmissions = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("PaymentTaskId")
    @Builder.Default
    private List<Payment> taskPayemtns = new ArrayList<>();

   @PrePersist
   protected void onCreated(){
    this.createdAt = LocalDateTime.now();
   }
}
