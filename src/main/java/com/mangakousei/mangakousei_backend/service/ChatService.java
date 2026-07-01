package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.ChatMessageRes;
import com.mangakousei.mangakousei_backend.dto.response.ConversationRes;
import com.mangakousei.mangakousei_backend.entity.entity.ChatMessage;
import com.mangakousei.mangakousei_backend.entity.entity.Conversation;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.ChatMessageRepository;
import com.mangakousei.mangakousei_backend.repository.ConversationRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int PREVIEW_MAX_LEN = 80;

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Conversation getOrCreateConversation(Long userId1, Long userId2) {
        if (userId1.equals(userId2)) {
            throw new CustomAppException(
                    "Không thể tạo conversation với chính mình", HttpStatus.BAD_REQUEST);
        }
        Long aId = Math.min(userId1, userId2);
        Long bId = Math.max(userId1, userId2);

        return conversationRepository
                .findByParticipantAUserIdAndParticipantBUserId(aId, bId)
                .orElseGet(() -> {
                    User a = userRepository.findById(aId)
                            .orElseThrow(() -> new CustomAppException(
                                    "Không tìm thấy user", HttpStatus.NOT_FOUND));
                    User b = userRepository.findById(bId)
                            .orElseThrow(() -> new CustomAppException(
                                    "Không tìm thấy user", HttpStatus.NOT_FOUND));

                    Conversation conv = Conversation.builder()
                            .participantA(a)
                            .participantB(b)
                            .build();

                    Conversation saved = conversationRepository.save(conv);
                    log.info("[Chat] Tạo conversation mới #{} giữa {} và {}",
                            saved.getConversationId(), a.getEmail(), b.getEmail());
                    return saved;
                });
    }

    @Transactional(readOnly = true)
    public List<ConversationRes> getMyConversations(Long userId) {
        return conversationRepository.findAllForUser(userId)
                .stream()
                .map(c -> toConversationRes(c, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageRes> getMessages(Long conversationId, Long userId, Pageable pageable) {
        Conversation conv = getConversationAndVerifyAccess(conversationId, userId);
        return messageRepository
                .findByConversationConversationIdOrderByCreatedAtDesc(conv.getConversationId(), pageable)
                .map(this::toMessageRes);
    }

    @Transactional
    public ChatMessageRes sendMessage(Long conversationId, Long senderId, String content) {
        Conversation conv = getConversationAndVerifyAccess(conversationId, senderId);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new CustomAppException("Không tìm thấy user", HttpStatus.NOT_FOUND));

        ChatMessage msg = ChatMessage.builder()
                .conversation(conv)
                .sender(sender)
                .content(content)
                .isRead(false)
                .build();
        messageRepository.save(msg);

        conv.setLastMessagePreview(truncate(content));
        conv.setLastMessageAt(msg.getCreatedAt());
        conversationRepository.save(conv);

        ChatMessageRes payload = toMessageRes(msg);

        String otherEmail = conv.getParticipantA().getUserId().equals(senderId)
                ? conv.getParticipantB().getEmail()
                : conv.getParticipantA().getEmail();

        pushRealtime(sender.getEmail(), payload);
        pushRealtime(otherEmail, payload);

        return payload;
    }

    @Transactional
    public void markConversationRead(Long conversationId, Long userId) {
        getConversationAndVerifyAccess(conversationId, userId); // đảm bảo có quyền
        messageRepository.markConversationReadForUser(conversationId, userId);
    }

    private Conversation getConversationAndVerifyAccess(Long conversationId, Long userId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy cuộc trò chuyện", HttpStatus.NOT_FOUND));

        boolean isParticipant = conv.getParticipantA().getUserId().equals(userId)
                || conv.getParticipantB().getUserId().equals(userId);

        if (!isParticipant) {
            throw new CustomAppException(
                    "Bạn không thuộc cuộc trò chuyện này", HttpStatus.FORBIDDEN);
        }
        return conv;
    }

    private void pushRealtime(String userEmail, ChatMessageRes payload) {
        try {
            messagingTemplate.convertAndSendToUser(userEmail, "/queue/messages", payload);
        } catch (Exception e) {
            log.warn("[Chat][Realtime] Push thất bại cho {}: {}", userEmail, e.getMessage());
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() <= PREVIEW_MAX_LEN ? s : s.substring(0, PREVIEW_MAX_LEN) + "...";
    }

    private ConversationRes toConversationRes(Conversation c, Long currentUserId) {
        boolean iAmA = c.getParticipantA().getUserId().equals(currentUserId);
        User other = iAmA ? c.getParticipantB() : c.getParticipantA();

        long unread = messageRepository
                .countByConversationConversationIdAndSenderUserIdNotAndIsReadFalse(
                        c.getConversationId(), currentUserId);

        return ConversationRes.builder()
                .conversationId(c.getConversationId())
                .otherUserId(other.getUserId())
                .otherUserName(other.getFullName())
                .otherUserAvatarUrl(other.getAvatarUrl())
                .otherUserRole(other.getRoles().isEmpty() ? null : other.getRoles().iterator().next().getRoleName())
                .lastMessagePreview(c.getLastMessagePreview())
                .lastMessageAt(c.getLastMessageAt())
                .unreadCount(unread)
                .build();
    }

    private ChatMessageRes toMessageRes(ChatMessage m) {
        return ChatMessageRes.builder()
                .messageId(m.getMessageId())
                .conversationId(m.getConversation().getConversationId())
                .senderId(m.getSender().getUserId())
                .senderName(m.getSender().getFullName())
                .senderAvatarUrl(m.getSender().getAvatarUrl())
                .content(m.getContent())
                .isRead(m.isRead())
                .createdAt(m.getCreatedAt())
                .build();
    }
}