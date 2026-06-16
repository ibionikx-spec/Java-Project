package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.ChangePasswordReq;
import com.mangakousei.mangakousei_backend.dto.request.UpdateProfileReq;
import com.mangakousei.mangakousei_backend.dto.response.UserFullProfileRes;
import com.mangakousei.mangakousei_backend.dto.response.UserInfoRes;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.mapper.UserMapper;
import com.mangakousei.mangakousei_backend.repository.ManuscriptRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import com.mangakousei.mangakousei_backend.dto.response.UserStatsRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
     private final SeriesRepository seriesRepository;
     private final ManuscriptRepository manuscriptRepository;
    @Transactional
    public UserInfoRes updateMyProfile(UpdateProfileReq request) {
        User user = getCurrentUser();

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getAvatarUrl() != null) {
            String avatarUrl = request.getAvatarUrl().trim();
            user.setAvatarUrl(avatarUrl.isEmpty() ? null : avatarUrl);
        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public void changeMyPassword(ChangePasswordReq request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new CustomAppException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User getCurrentUser() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomAppException("User not found", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserInfoRes getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomAppException("User not found", HttpStatus.NOT_FOUND));
        return userMapper.toDto(user);
    }
    @Transactional(readOnly = true)
    public UserStatsRes getUserStats(Long userId){
        if (!userRepository.existsById(userId)) {
            throw new CustomAppException("User not found", HttpStatus.NOT_FOUND);
        }
        long createdSeriesCount = seriesRepository.countByCreatorUserId(userId);
        long editedSeriesCount = seriesRepository.countByEditorUserId(userId);
        long manuscriptCount = manuscriptRepository.countBySubmitterUserId(userId);
        long totalChaptersCreated = seriesRepository.countChaptersByCreatorUserId(userId);
        long totalPagesCreated = seriesRepository.countPagesByCreatorUserId(userId);
        return UserStatsRes.builder()
            .createdSeriesCount(createdSeriesCount)
            .editedSeriesCount(editedSeriesCount)
            .manuscriptCount(manuscriptCount)
            .totalPagesCreated(totalPagesCreated)
            .totalChaptersCreated(totalChaptersCreated)
            .build();
    }
    public String extractPublicId(String url) {
        String path = url.substring(url.indexOf("/upload/") + 8);
        path = path.replaceFirst("^v\\d+/", "");
        return path.substring(0, path.lastIndexOf("."));
    }

    @Transactional
    public String updateAvatar(MultipartFile file) {
       User user = getCurrentUser();
    String oldAvatarUrl = user.getAvatarUrl();
    String newAvatarUrl = cloudinaryService.uploadAvatar(file);
    user.setAvatarUrl(newAvatarUrl);
    userRepository.save(user);
    if (oldAvatarUrl != null && !oldAvatarUrl.isBlank()) {
        try {
            String publicId = extractPublicId(oldAvatarUrl);
            cloudinaryService.deleteImage(publicId);
        } catch (Exception e) {
            throw e;
        }
    }
    return newAvatarUrl;
    }
    @Transactional(readOnly = true)
        public UserFullProfileRes getUserFullProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomAppException("User not found", HttpStatus.NOT_FOUND));
        return userMapper.toFullProfileDto(user);
    }
}