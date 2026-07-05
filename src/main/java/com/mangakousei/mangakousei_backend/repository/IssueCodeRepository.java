package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.system.IssueCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IssueCodeRepository extends JpaRepository<IssueCode, Long> {
    Optional<IssueCode> findByIssueCodeName(String issueCodeName);
}
