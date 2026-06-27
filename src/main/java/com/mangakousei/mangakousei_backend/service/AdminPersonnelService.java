package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.AdminCreateUserReq;
import com.mangakousei.mangakousei_backend.dto.request.LogContext;
import com.mangakousei.mangakousei_backend.dto.response.*;
import com.mangakousei.mangakousei_backend.entity.entity.*;
import com.mangakousei.mangakousei_backend.entity.system.Role;
import com.mangakousei.mangakousei_backend.entity.type.ActionType;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.*;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPersonnelService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TantouMangakaAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<PersonnelUserRes> getUsersByRole(String roleName) {
        return userRepository.findAllByRoleName(roleName)
                .stream()
                .map(this::toPersonnelRes)
                .collect(Collectors.toList());
    }

    @Transactional
    public PersonnelUserRes createUser(AdminCreateUserReq req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent())
            throw new CustomAppException("Email đã tồn tại", HttpStatus.CONFLICT);

        Role role = roleRepository.findByRoleName(req.getRoleName())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy role: " + req.getRoleName(), HttpStatus.BAD_REQUEST));

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .roles(List.of(role))
                .build();

        User saved = userRepository.save(user);

        Long adminId = SecurityUtils.getCurrentUserId();
        activityLogService.log(adminId, LogContext.builder()
                .actionType(ActionType.CREATE_USER)
                .detail("Admin tạo tài khoản " + req.getRoleName()
                        + ": " + saved.getFullName() + " (" + saved.getEmail() + ")")
                .entityType("USER")
                .entityId(saved.getUserId())
                .build());

        notificationService.send(saved.getUserId(), "SYSTEM",
                "🎉 Tài khoản của bạn đã được tạo",
                "Chào " + saved.getFullName() + "! Admin vừa tạo tài khoản "
                        + req.getRoleName() + " cho bạn. Hãy đăng nhập bằng email này.");

        return toPersonnelRes(saved);
    }

    @Transactional(readOnly = true)
    public List<TantouMangakaAssignRes> getAllAssignments() {
        return assignmentRepository.findAll()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsActive()))
                .map(this::toAssignRes)
                .collect(Collectors.toList());
    }

    @Transactional
    public TantouMangakaAssignRes assignTantouToMangaka(Long tantouId, Long mangakaId) {
        Long adminId = SecurityUtils.getCurrentUserId();

        User tantou = userRepository.findById(tantouId)
                .orElseThrow(() -> new CustomAppException("Không tìm thấy Tantou", HttpStatus.NOT_FOUND));
        User mangaka = userRepository.findById(mangakaId)
                .orElseThrow(() -> new CustomAppException("Không tìm thấy Mangaka", HttpStatus.NOT_FOUND));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomAppException("Không tìm thấy Admin", HttpStatus.NOT_FOUND));

        TantouMangakaAssignment assignment = assignmentRepository
                .findByTantouUserIdAndMangakaUserId(tantouId, mangakaId)
                .orElse(TantouMangakaAssignment.builder()
                        .tantou(tantou)
                        .mangaka(mangaka)
                        .build());

        assignment.setAssignedBy(admin);
        assignment.setIsActive(true);

        TantouMangakaAssignment saved = assignmentRepository.save(assignment);

        activityLogService.log(adminId, LogContext.builder()
                .actionType(ActionType.ASSIGN_TANTOU)
                .detail("Assign Tantou " + tantou.getFullName()
                        + " → Mangaka " + mangaka.getFullName())
                .entityType("ASSIGNMENT")
                .entityId(saved.getAssignmentId())
                .build());

        notificationService.send(tantouId, "SYSTEM",
                "📋 Phân công Mangaka mới",
                "Bạn đã được phân công quản lý Mangaka: " + mangaka.getFullName()
                        + ". Hãy liên hệ và bắt đầu cộng tác!");
        notificationService.send(mangakaId, "SYSTEM",
                "📋 Tantou phụ trách mới",
                "Admin vừa chỉ định " + tantou.getFullName()
                        + " làm Tantou phụ trách cho bạn.");

        return toAssignRes(saved);
    }

    @Transactional
    public void removeAssignment(Long assignmentId) {
        Long adminId = SecurityUtils.getCurrentUserId();

        TantouMangakaAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy assignment", HttpStatus.NOT_FOUND));

        assignment.setIsActive(false);
        assignmentRepository.save(assignment);

        activityLogService.log(adminId, LogContext.builder()
                .actionType(ActionType.REMOVE_ASSIGNMENT)
                .detail("Hủy assign Tantou " + assignment.getTantou().getFullName()
                        + " – Mangaka " + assignment.getMangaka().getFullName())
                .entityType("ASSIGNMENT")
                .entityId(assignmentId)
                .build());

        notificationService.send(assignment.getTantou().getUserId(), "SYSTEM",
                "🔔 Thay đổi phân công",
                "Bạn không còn phụ trách Mangaka "
                        + assignment.getMangaka().getFullName() + " nữa.");
        notificationService.send(assignment.getMangaka().getUserId(), "SYSTEM",
                "🔔 Thay đổi phân công",
                "Admin đã thay đổi Tantou phụ trách của bạn.");
    }

    private PersonnelUserRes toPersonnelRes(User u) {
        return PersonnelUserRes.builder()
                .userId(u.getUserId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .avatarUrl(u.getAvatarUrl())
                .roles(u.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList()))
                .createdAt(u.getCreatedAt())
                .build();
    }

    private TantouMangakaAssignRes toAssignRes(TantouMangakaAssignment a) {
        return TantouMangakaAssignRes.builder()
                .assignmentId(a.getAssignmentId())
                .tantouId(a.getTantou().getUserId())
                .tantouName(a.getTantou().getFullName())
                .tantouAvatarUrl(a.getTantou().getAvatarUrl())
                .mangakaId(a.getMangaka().getUserId())
                .mangakaName(a.getMangaka().getFullName())
                .mangakaAvatarUrl(a.getMangaka().getAvatarUrl())
                .assignedAt(a.getAssignedAt())
                .isActive(a.getIsActive())
                .build();
    }
}