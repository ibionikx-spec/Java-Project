package com.mangakousei.mangakousei_backend.repository;

import com.mangakousei.mangakousei_backend.entity.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Long> {
    List<Page> findByChapterChapterIdOrderByPageNumberAsc(Long chapterId);
    Optional<Page> findByChapterChapterIdAndPageNumber(Long chapterId, int pageNumber);
    long countByChapterChapterId(Long chapterId);
}
