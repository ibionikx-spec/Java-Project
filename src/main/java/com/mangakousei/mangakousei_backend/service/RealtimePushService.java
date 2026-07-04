package com.mangakousei.mangakousei_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimePushService {

    private final SimpMessagingTemplate messagingTemplate;

    public void pushToUser(String userEmail, String destination, Object payload) {
        if (userEmail == null) return;
        try {
            messagingTemplate.convertAndSendToUser(userEmail, destination, payload);
        } catch (Exception e) {
            log.warn("[Realtime][{}] Push thất bại cho {}: {}", destination, userEmail, e.getMessage());
        }
    }
}