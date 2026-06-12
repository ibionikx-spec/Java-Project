package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.ProposalGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalGenreRepository extends JpaRepository<ProposalGenre, Long> {

    @Query("SELECT pg.proposal.proposalId, g.genreName " +
            "FROM ProposalGenre pg JOIN pg.genre g " +
            "WHERE pg.proposal.proposalId IN :proposalIds")
    List<Object[]> findGenreNamesByProposalIds(@Param("proposalIds") List<Long> proposalIds);
}