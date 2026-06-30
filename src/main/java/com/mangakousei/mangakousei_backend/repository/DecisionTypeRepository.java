package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.type.DecisionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DecisionTypeRepository extends JpaRepository<DecisionType, Long> {
    Optional<DecisionType> findByDecisionTypeName(String decisionTypeName);
}
