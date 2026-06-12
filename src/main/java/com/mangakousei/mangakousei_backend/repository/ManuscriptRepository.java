package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.dto.response.InboxItemRes;
import com.mangakousei.mangakousei_backend.entity.entity.Manuscript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManuscriptRepository extends JpaRepository<Manuscript, Long> {

    @Query("SELECT new com.mangakousei.mangakousei_backend.dto.response.InboxItemRes(" +
            "'manuscript', m.manuscriptId, s.title, " +
            "CONCAT(mt.typeName, ' - Ch.', c.chapterNumber), " +
            "u.fullName, m.submittedAt, " +
            "'pending', 'CHỜ DUYỆT') " +
            "FROM Manuscript m " +
            "JOIN m.chapter c " +
            "JOIN c.series s " +
            "JOIN m.submitter u " +
            "JOIN m.manuscriptStatus ms " +
            "JOIN m.manuscriptType mt " +
            "WHERE ms.manuscriptName = 'submitted'")
    List<InboxItemRes> findSubmittedManuscripts();
}