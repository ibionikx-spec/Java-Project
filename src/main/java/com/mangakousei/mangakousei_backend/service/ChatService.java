package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.constant.RealtimeQueues;
import com.mangakousei.mangakousei_backend.dto.response.AdminContactRes;
import com.mangakousei.mangakousei_backend.dto.response.ChatMessageRes;
import com.mangakousei.mangakousei_backend.dto.response.ConversationRes;
import com.mangakousei.mangakousei_backend.entity.entity.ChatMessage;
import com.mangakousei.mangakousei_backend.entity.entity.Conversation;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.ChatMessageRepository;
import com.mangakousei.mangakousei_backend.repository.ConversationRepository;
import com.mangakousei.mangakousei_backend.repository.MangakaAssistantAssignmentRepository;
import com.mangakousei.mangakousei_backend.repository.TantouMangakaAssignmentRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int PREVIEW_MAX_LEN = 80;
    private static final String ASSISTANT_ACTIVE_STATUS = "active";
    private static final String ADMIN_ROLE = "ADMIN";

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final RealtimePushService realtimePushService;
    private final TantouMangakaAssignmentRepository tantouMangakaAssignmentRepository;
    private final MangakaAssistantAssignmentRepository mangakaAssistantAssignmentRepository;

    
}
