package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.UserInfoRes;
import com.mangakousei.mangakousei_backend.entity.entity.TantouMangakaAssignment;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.mapper.UserMapper;
import com.mangakousei.mangakousei_backend.repository.TantouMangakaAssignmentRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TantouMangakaService {

    private final TantouMangakaAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserInfoRes> getActiveTantousForMangaka(Long mangakaId) {
        User mangaka = userRepository.findById(mangakaId)
            .orElseThrow(() -> new RuntimeException("Mangaka not found with id: " + mangakaId));
        
        // if (!mangaka.hasRole("MANGAKA")) {
        //     throw new BadRequestException("User with id " + mangakaId + " is not a MANGAKA");
        // }
        
        List<TantouMangakaAssignment> assignments = assignmentRepository
            .findByMangaka_UserIdAndIsActiveTrue(mangakaId);
        
        return assignments.stream()
            .map(assignment -> userMapper.toDto(assignment.getTantou()))
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserInfoRes> getActiveMangakasForTantou(Long tantouId) {
        User tantou = userRepository.findById(tantouId)
            .orElseThrow(() -> new RuntimeException("Tantou not found"));
        
        // if (!tantou.hasRole("TANTOU")) {
        //     throw new BadRequestException("User is not a TANTOU");
        // }
        
        List<TantouMangakaAssignment> assignments = assignmentRepository
            .findByTantou_UserIdAndIsActiveTrue(tantouId);
        
        return assignments.stream()
            .map(assignment -> userMapper.toDto(assignment.getMangaka()))
            .collect(Collectors.toList());
    }
}
