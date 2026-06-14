
package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.repository.TantouMangakaAssignmentRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final TantouMangakaAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public TantouMangakaAssignment assignTantouToMangaka(
            Long tantouId, 
            Long mangakaId, 
            Long adminId
            ) {
        User tantou = userRepository.findById(tantouId)
            .orElseThrow(() -> new RuntimeException("Tantou not found"));
        User mangaka = userRepository.findById(mangakaId)
            .orElseThrow(() -> new RuntimeException("Mangaka not found"));
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        // if (!tantou.hasRole("TANTOU") || !mangaka.hasRole("MANGAKA") || !admin.hasRole("ADMIN")) {
        //     throw new BadRequestException("Role mismatch");
        // }
        assignmentRepository.deactivateAllFor(tantouId, mangakaId);       
        
        TantouMangakaAssignment assignment = TantouMangakaAssignment.builder()
            .tantou(tantou)
            .mangaka(mangaka)
            .assignedBy(admin)
            .isActive(true)
            .build();
        
        return assignmentRepository.save(assignment);
    }

    public List<TantouMangakaAssignment> getActiveMangakasForTantou(Long tantouId) {
        return assignmentRepository.findByTantou_UserIdAndIsActiveTrue(tantouId);
    }

    public List<TantouMangakaAssignment> getActiveTantousForMangaka(Long mangakaId) {
        return assignmentRepository.findByMangaka_UserIdAndIsActiveTrue(mangakaId);
    }
}
