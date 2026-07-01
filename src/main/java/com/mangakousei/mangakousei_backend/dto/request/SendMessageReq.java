package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SendMessageReq {

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    @Size(max = 4000, message = "Tin nhắn tối đa 4000 ký tự")
    private String content;
}