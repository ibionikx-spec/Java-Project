package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.type.PublicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublicationTypeRepository extends JpaRepository<PublicationType, Long> {
    Optional<PublicationType> findByPublicationTypeName(String publicationTypeName);
}
