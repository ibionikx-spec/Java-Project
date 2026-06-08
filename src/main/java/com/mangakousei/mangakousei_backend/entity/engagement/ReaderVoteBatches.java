package com.mangakousei.mangakousei_backend.entity.engagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.entity.system.IssueCode;

@Entity
@Table(name = "reader_vote_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReaderVoteBatches {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long batchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_code_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private IssueCode issueCode;

    @Column(name = "note", nullable = false, columnDefinition = "TEXT")
    private String note;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imported_by")
    @JsonBackReference("importedVoteBatches")
    @ToString.Exclude
    private User importer;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("ReaderVoteBatches")
        @Builder.Default
    private List<ReaderVote> votes = new ArrayList<>();

    @PrePersist
    protected void onImported(){
        this.importedAt = LocalDateTime.now();
    }
}
