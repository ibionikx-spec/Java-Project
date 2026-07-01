package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.SendMessageReq;
import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.ChatMessageRes;
import com.mangakousei.mangakousei_backend.service.ChatService;
import com.mangakousei.mangakousei_backend.util.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<?> getMyConversations() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                "Fetched conversations", chatService.getMyConversations(userId)));
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable("id") Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessageRes> messages = chatService.getMessages(conversationId, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Fetched messages", messages));
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable("id") Long conversationId,
            @Valid @RequestBody SendMessageReq req
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        ChatMessageRes sent = chatService.sendMessage(conversationId, userId, req.getContent());
        return ResponseEntity.ok(ApiResponse.success("Message sent", sent));
    }

    @PatchMapping("/conversations/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable("id") Long conversationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        chatService.markConversationRead(conversationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }
}