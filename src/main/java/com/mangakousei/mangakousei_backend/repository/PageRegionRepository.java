package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.PageRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PageRegionRepository extends JpaRepository<PageRegion, Long> {
    List<PageRegion> findByPagePageId(Long pageId);
}