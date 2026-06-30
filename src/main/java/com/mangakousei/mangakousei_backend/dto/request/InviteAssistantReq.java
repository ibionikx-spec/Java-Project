package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteAssistantReq {
    @NotNull
    private Long assistantId;
}