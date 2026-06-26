package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.CreateProposalReq;
import com.mangakousei.mangakousei_backend.dto.request.ReviewProposalReq;
import com.mangakousei.mangakousei_backend.dto.response.ProposalRes;
import com.mangakousei.mangakousei_backend.entity.entity.SeriesProposal;
import com.mangakousei.mangakousei_backend.entity.entity.TantouMangakaAssignment;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.*;
import com.mangakousei.mangakousei_backend.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeriesProposalService Tests")
class SeriesProposalServiceTest {

    @Mock private SeriesProposalRepository proposalRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private UserRepository userRepository;
    @Mock private TantouMangakaAssignmentRepository assignmentRepository;
    @Mock private SeriesRepository seriesRepository;
    @Mock private SeriesStatusRepository seriesStatusRepository;
    @Mock private PublicationTypeRepository publicationTypeRepository;
    @Mock private DecisionTypeRepository decisionTypeRepository;
    @Mock private PublicationDecisionRepository publicationDecisionRepository;
    @Mock private PublicationScheduleRepository publicationScheduleRepository;

    @InjectMocks
    private SeriesProposalService seriesProposalService;

    private User mockMangaka;
    private User mockTantou;

    @BeforeEach
    void setUp() {
        mockMangaka = User.builder()
                .userId(1L)
                .fullName("Mangaka Test")
                .email("mangaka@test.com")
                .passwordHash("hashed")
                .build();

        mockTantou = User.builder()
                .userId(2L)
                .fullName("Tantou Test")
                .email("tantou@test.com")
                .passwordHash("hashed")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "mangaka@test.com", "hashed", "Mangaka Test", null,
                List.of(new SimpleGrantedAuthority("MANGAKA"))
        );
        SecurityContextHolder.setContext(new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        ));
    }

    @Test
    @DisplayName("createProposal: thành công khi mangaka được phân công cho tantou")
    void createProposal_whenAssigned_shouldReturnProposalRes() {
        CreateProposalReq req = buildCreateReq();

        when(userRepository.findByEmail("mangaka@test.com")).thenReturn(Optional.of(mockMangaka));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockTantou));
        when(assignmentRepository.findByTantou_UserIdAndMangaka_UserIdAndIsActiveTrue(2L, 1L))
                .thenReturn(Optional.of(mock(TantouMangakaAssignment.class)));

        SeriesProposal saved = SeriesProposal.builder()
                .proposalId(10L)
                .status("pending")
                .build();
        when(proposalRepository.save(any())).thenReturn(saved);

        ProposalRes result = seriesProposalService.createProposal(req);

        assertThat(result.getProposalId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo("pending");
        verify(proposalRepository).save(any(SeriesProposal.class));
    }

    @Test
    @DisplayName("createProposal: tantou không tồn tại → ném BAD_REQUEST")
    void createProposal_whenTantouNotFound_shouldThrow() {
        CreateProposalReq req = buildCreateReq();

        when(userRepository.findByEmail("mangaka@test.com")).thenReturn(Optional.of(mockMangaka));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seriesProposalService.createProposal(req))
                .isInstanceOf(CustomAppException.class)
                .satisfies(ex -> assertThat(((CustomAppException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(proposalRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProposal: mangaka không được phân công cho tantou → ném FORBIDDEN")
    void createProposal_whenNotAssigned_shouldThrowForbidden() {
        CreateProposalReq req = buildCreateReq();

        when(userRepository.findByEmail("mangaka@test.com")).thenReturn(Optional.of(mockMangaka));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockTantou));
        when(assignmentRepository.findByTantou_UserIdAndMangaka_UserIdAndIsActiveTrue(2L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> seriesProposalService.createProposal(req))
                .isInstanceOf(CustomAppException.class)
                .satisfies(ex -> assertThat(((CustomAppException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));

        verify(proposalRepository, never()).save(any());
    }

    @Test
    @DisplayName("reviewProposal: approve → status chuyển thành pending_admin")
    void reviewProposal_approve_shouldSetPendingAdmin() {
        SeriesProposal proposal = buildProposal("pending");
        ReviewProposalReq req = new ReviewProposalReq();
        req.setDecision("approve");

        when(userRepository.findByEmail("mangaka@test.com")).thenReturn(Optional.of(mockMangaka));
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any())).thenReturn(proposal);

        seriesProposalService.reviewProposal(1L, req);

        assertThat(proposal.getStatus()).isEqualTo("pending_admin");
        verify(proposalRepository).save(proposal);
    }

    @Test
    @DisplayName("reviewProposal: revision không có feedback → ném BAD_REQUEST")
    void reviewProposal_revision_withoutFeedback_shouldThrow() {
        SeriesProposal proposal = buildProposal("pending");
        ReviewProposalReq req = new ReviewProposalReq();
        req.setDecision("revision");
        req.setFeedback("");

        when(userRepository.findByEmail("mangaka@test.com")).thenReturn(Optional.of(mockMangaka));
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));

        assertThatThrownBy(() -> seriesProposalService.reviewProposal(1L, req))
                .isInstanceOf(CustomAppException.class)
                .satisfies(ex -> assertThat(((CustomAppException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("reviewProposal: revision có feedback → status chuyển thành revision")
    void reviewProposal_revision_withFeedback_shouldSetRevision() {
        SeriesProposal proposal = buildProposal("pending");
        ReviewProposalReq req = new ReviewProposalReq();
        req.setDecision("revision");
        req.setFeedback("Cần sửa lại nhân vật chính");

        when(userRepository.findByEmail("mangaka@test.com")).thenReturn(Optional.of(mockMangaka));
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any())).thenReturn(proposal);

        seriesProposalService.reviewProposal(1L, req);

        assertThat(proposal.getStatus()).isEqualTo("revision");
        assertThat(proposal.getRevisionFeedback()).isEqualTo("Cần sửa lại nhân vật chính");
    }

    @Test
    @DisplayName("reviewProposal: reject không có reason → ném BAD_REQUEST")
    void reviewProposal_reject_withoutReason_shouldThrow() {
        SeriesProposal proposal = buildProposal("pending");
        ReviewProposalReq req = new ReviewProposalReq();
        req.setDecision("reject");
        req.setReason("");

        when(userRepository.findByEmail("mangaka@test.com")).thenReturn(Optional.of(mockMangaka));
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));

        assertThatThrownBy(() -> seriesProposalService.reviewProposal(1L, req))
                .isInstanceOf(CustomAppException.class)
                .satisfies(ex -> assertThat(((CustomAppException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("reviewProposal: proposal không tồn tại → ném NOT_FOUND")
    void reviewProposal_notFound_shouldThrow() {
        ReviewProposalReq req = new ReviewProposalReq();
        req.setDecision("approve");

        when(proposalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seriesProposalService.reviewProposal(99L, req))
                .isInstanceOf(CustomAppException.class)
                .satisfies(ex -> assertThat(((CustomAppException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("reviewProposal: proposal đã approved → ném BAD_REQUEST")
    void reviewProposal_alreadyApproved_shouldThrow() {
        SeriesProposal proposal = buildProposal("approved");
        ReviewProposalReq req = new ReviewProposalReq();
        req.setDecision("approve");

        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));

        assertThatThrownBy(() -> seriesProposalService.reviewProposal(1L, req))
                .isInstanceOf(CustomAppException.class)
                .satisfies(ex -> assertThat(((CustomAppException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("adminReviewProposal: approve → status chuyển thành approved_pending_schedule")
    void adminReviewProposal_approve_shouldSetApprovedPendingSchedule() {
        SeriesProposal proposal = buildProposal("pending_admin");
        ReviewProposalReq req = new ReviewProposalReq();
        req.setDecision("approve");

        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any())).thenReturn(proposal);

        seriesProposalService.adminReviewProposal(1L, req);

        assertThat(proposal.getStatus()).isEqualTo("approved_pending_schedule");
    }

    @Test
    @DisplayName("adminReviewProposal: reject có reason → status chuyển thành rejected")
    void adminReviewProposal_reject_shouldSetRejected() {
        SeriesProposal proposal = buildProposal("pending_admin");
        ReviewProposalReq req = new ReviewProposalReq();
        req.setDecision("reject");
        req.setReason("Nội dung không phù hợp");

        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any())).thenReturn(proposal);

        seriesProposalService.adminReviewProposal(1L, req);

        assertThat(proposal.getStatus()).isEqualTo("rejected");
        assertThat(proposal.getRejectionReason()).isEqualTo("Nội dung không phù hợp");
    }

    @Test
    @DisplayName("adminReviewProposal: proposal không ở trạng thái pending_admin → ném BAD_REQUEST")
    void adminReviewProposal_wrongStatus_shouldThrow() {
        SeriesProposal proposal = buildProposal("pending");
        ReviewProposalReq req = new ReviewProposalReq();
        req.setDecision("approve");

        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));

        assertThatThrownBy(() -> seriesProposalService.adminReviewProposal(1L, req))
                .isInstanceOf(CustomAppException.class)
                .satisfies(ex -> assertThat(((CustomAppException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("reopenProposal: từ rejected → status về pending")
    void reopenProposal_fromRejected_shouldSetPending() {
        SeriesProposal proposal = buildProposal("rejected");

        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any())).thenReturn(proposal);

        seriesProposalService.reopenProposal(1L);

        assertThat(proposal.getStatus()).isEqualTo("pending");
    }

    @Test
    @DisplayName("reopenProposal: proposal không tồn tại → ném NOT_FOUND")
    void reopenProposal_notFound_shouldThrow() {
        when(proposalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seriesProposalService.reopenProposal(99L))
                .isInstanceOf(CustomAppException.class)
                .satisfies(ex -> assertThat(((CustomAppException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    private CreateProposalReq buildCreateReq() {
        CreateProposalReq req = new CreateProposalReq();
        req.setTantouId(2L);
        req.setWorkingTitle("Test Series");
        req.setSynopsis("Đây là synopsis test");
        req.setTargetAudience("Shounen");
        req.setGenreIds(List.of());
        req.setCharacters(List.of());
        return req;
    }

    private SeriesProposal buildProposal(String status) {
        return SeriesProposal.builder()
                .proposalId(1L)
                .mangaka(mockMangaka)
                .workingTitle("Test Series")
                .synopsis("Synopsis")
                .targetAudience("Shounen")
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}