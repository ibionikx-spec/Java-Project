package com.mangakousei.mangakousei_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TantouMangakaAssignRes {
    private Long   assignmentId;
    private Long   tantouId;
    private String tantouName;
    private String tantouAvatarUrl;
    private Long   mangakaId;
    private String mangakaName;
    private String mangakaAvatarUrl;
    private Boolean isActive;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime assignedAt;
}