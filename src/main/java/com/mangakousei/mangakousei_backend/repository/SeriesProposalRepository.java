package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.dto.response.InboxItemRes;
import com.mangakousei.mangakousei_backend.entity.entity.SeriesProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = "SELECT " +
            "sp.proposal_id, sp.working_title, sp.synopsis, sp.target_audience, " +
            "sp.status, sp.created_at, sp.name_summary, sp.rejection_reason, sp.revision_feedback, sp.sketch_image_url, " +
            "u.user_id AS mangaka_user_id, u.full_name AS mangaka_full_name, u.avatar_url AS mangaka_avatar_url, " +
            "COALESCE((" +
            "   SELECT json_agg(json_build_object('genre_id', g.genre_id, 'name', g.genre_name)) " +
            "   FROM proposal_genres pg JOIN genres g ON pg.genre_id = g.genre_id " +
            "   WHERE pg.proposal_id = sp.proposal_id" +
            "), '[]'::json) AS genres_json, " +
            "COALESCE((" +
            "   SELECT json_agg(json_build_object('character_id', pc.character_id, 'character_name', pc.character_name, 'role', pc.role, 'description', pc.description)) " +
            "   FROM proposal_characters pc " +
            "   WHERE pc.proposal_id = sp.proposal_id" +
            "), '[]'::json) AS characters_json " +
            "FROM series_proposals sp " +
            "JOIN users u ON sp.mangaka_id = u.user_id " +
            "WHERE (CAST(:status AS varchar) IS NULL OR sp.status = CAST(:status AS varchar)) " +
            "AND (CAST(:search AS text) IS NULL OR " +
            "sp.working_title ILIKE CONCAT('%', CAST(:search AS text), '%') " +
            "OR u.full_name ILIKE CONCAT('%', CAST(:search AS text), '%'))",
            nativeQuery = true)
    List<Object[]> findProposalsRaw(@Param("status") String status, @Param("search") String search);
}