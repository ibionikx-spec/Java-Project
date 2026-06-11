package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.dto.response.InboxItemRes;
import com.mangakousei.mangakousei_backend.entity.entity.SeriesProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeriesProposalRepository extends JpaRepository<SeriesProposal, Long> {
    @Query("SELECT new com.mangakousei.mangakousei_backend.dto.response.InboxItemRes(" +
            "'proposal', sp.proposalId, NULL, " +
            "CONCAT('Đề xuất: ', sp.workingTitle), " +
            "u.fullName, sp.createdAt, " +
            "sp.status, " +
            "CASE sp.status " +
            "   WHEN 'pending' THEN 'CHỜ DUYỆT' " +
            "   WHEN 'revision_requested' THEN 'YÊU CẦU SỬA' " +
            "   ELSE sp.status END) " +
            "FROM SeriesProposal sp JOIN sp.mangaka u " +
            "WHERE sp.status IN ('pending', 'revision_requested')")
    List<InboxItemRes> findPendingProposals();
}