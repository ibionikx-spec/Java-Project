package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.type.RegionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RegionTypeRepository extends JpaRepository<RegionType, Long> {
    Optional<RegionType> findByRegionTypeName(String regionTypeName);
}