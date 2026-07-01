package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.InviteAssistantReq;
import com.mangakousei.mangakousei_backend.dto.request.RespondInvitationReq;
import com.mangakousei.mangakousei_backend.dto.response.AssistantAssignmentRes;
import com.mangakousei.mangakousei_backend.dto.response.AssistantSearchRes;
import com.mangakousei.mangakousei_backend.entity.entity.MangakaAssistantAssignment;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.MangakaAssistantAssignmentRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MangakaAssistantService {

    private final MangakaAssistantAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;

    public List<AssistantSearchRes> searchAssistants(String keyword, Long mangakaId) {
        List<User> assistants = userRepository
                .findByRoleNameAndKeyword("ASSISTANT", keyword.trim().toLowerCase());

        return assistants.stream()
                .map(a -> {
                    Optional<MangakaAssistantAssignment> latest =
                            assignmentRepository
                                    .findTopByMangakaUserIdAndAssistantUserIdOrderByInvitedAtDesc(
                                            mangakaId, a.getUserId());

                    String relStatus = latest.map(MangakaAssistantAssignment::getStatus)
                            .orElse(null);

                    return AssistantSearchRes.builder()
                            .userId(a.getUserId())
                            .fullName(a.getFullName())
                            .email(a.getEmail())
                            .avatarUrl(a.getAvatarUrl())
                            .relationshipStatus(relStatus)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public AssistantAssignmentRes inviteAssistant(InviteAssistantReq req, Long mangakaId) {
        User mangaka = getUserById(mangakaId);
        User assistant = getUserById(req.getAssistantId());

        Optional<MangakaAssistantAssignment> existing =
                assignmentRepository
                        .findTopByMangakaUserIdAndAssistantUserIdOrderByInvitedAtDesc(
                                mangakaId, req.getAssistantId());

        if (existing.isPresent() && "pending".equals(existing.get().getStatus())) {
            throw new CustomAppException(
                    "Đã có lời mời đang chờ assistant này phản hồi",
                    HttpStatus.CONFLICT);
        }

        if (existing.isPresent() && "active".equals(existing.get().getStatus())) {
            throw new CustomAppException(
                    "Assistant này đang trong nhóm của bạn",
                    HttpStatus.CONFLICT);
        }

        MangakaAssistantAssignment assignment = MangakaAssistantAssignment.builder()
                .mangaka(mangaka)
                .assistant(assistant)
                .status("pending")
                .build();

        return toRes(assignmentRepository.save(assignment));
    }

    public List<AssistantAssignmentRes> getActiveAssistants(Long mangakaId) {
        return assignmentRepository
                .findByMangakaUserIdAndStatus(mangakaId, "active")
                .stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    public List<AssistantAssignmentRes> getPendingInvitations(Long mangakaId) {
        return assignmentRepository
                .findByMangakaUserIdOrderByInvitedAtDesc(mangakaId)
                .stream()
                .filter(a -> "pending".equals(a.getStatus()))
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivate(Long assignmentId, Long actorId) {
        MangakaAssistantAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy assignment", HttpStatus.NOT_FOUND));

        if (!"active".equals(assignment.getStatus())
                && !"pending".equals(assignment.getStatus())) {
            throw new CustomAppException(
                    "Assignment này không thể ngắt", HttpStatus.BAD_REQUEST);
        }

        User actor = getUserById(actorId);
        assignment.setStatus("inactive");
        assignment.setDeactivatedBy(actor);
        assignment.setDeactivatedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);
    }

    public List<AssistantAssignmentRes> getMyInvitations(Long assistantId) {
        return assignmentRepository
                .findByAssistantUserIdAndStatus(assistantId, "pending")
                .stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    public long countPendingInvitations(Long assistantId) {
        return assignmentRepository.countByAssistantUserIdAndStatus(assistantId, "pending");
    }

    @Transactional
    public AssistantAssignmentRes respondToInvitation(Long assignmentId,
                                                      RespondInvitationReq req,
                                                      Long assistantId) {
        MangakaAssistantAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy lời mời", HttpStatus.NOT_FOUND));

        if (!assignment.getAssistant().getUserId().equals(assistantId)) {
            throw new CustomAppException(
                    "Bạn không có quyền phản hồi lời mời này", HttpStatus.FORBIDDEN);
        }

        if (!"pending".equals(assignment.getStatus())) {
            throw new CustomAppException(
                    "Lời mời này đã được xử lý rồi", HttpStatus.BAD_REQUEST);
        }

        switch (req.getDecision()) {
            case "accept" -> {
                assignment.setStatus("active");
                chatService.getOrCreateConversation(
                        assignment.getMangaka().getUserId(),
                        assignment.getAssistant().getUserId()
                );
            }
            case "reject" -> assignment.setStatus("rejected");
            default -> throw new CustomAppException(
                    "Decision không hợp lệ: chỉ chấp nhận 'accept' hoặc 'reject'",
                    HttpStatus.BAD_REQUEST);
        }

        assignment.setRespondedAt(LocalDateTime.now());
        return toRes(assignmentRepository.save(assignment));
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy user", HttpStatus.NOT_FOUND));
    }

    private AssistantAssignmentRes toRes(MangakaAssistantAssignment a) {
        return AssistantAssignmentRes.builder()
                .assignmentId(a.getAssignmentId())
                .assistantId(a.getAssistant().getUserId())
                .assistantName(a.getAssistant().getFullName())
                .assistantAvatarUrl(a.getAssistant().getAvatarUrl())
                .assistantEmail(a.getAssistant().getEmail())
                .mangakaId(a.getMangaka().getUserId())
                .mangakaName(a.getMangaka().getFullName())
                .mangakaAvatarUrl(a.getMangaka().getAvatarUrl())
                .status(a.getStatus())
                .invitedAt(a.getInvitedAt())
                .respondedAt(a.getRespondedAt())
                .build();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new CustomAppException(
                        "User not found", HttpStatus.NOT_FOUND));
    }
}