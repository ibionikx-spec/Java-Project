package com.mangakousei.mangakousei_backend.entity.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "proposal_genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ProposalGenre.ProposalGenreId.class)
public class ProposalGenre {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    @JsonBackReference("ProposalGenres")
    private SeriesProposal proposal;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposalGenreId implements Serializable {
        private Long proposal;
        private Long genre;
    }
}