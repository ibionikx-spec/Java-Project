package com.mangakousei.mangakousei_backend.entity.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "proposal_characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "character_id")
    private Long characterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    @JsonBackReference("ProposalCharacters")
    private SeriesProposal proposal;

    @Column(name = "character_name", nullable = false, length = 255)
    private String characterName;

    @Column(name = "role", nullable = false, length = 255)
    private String role;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}