package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RespondInvitationReq {
    @NotBlank
    private String decision;
}
