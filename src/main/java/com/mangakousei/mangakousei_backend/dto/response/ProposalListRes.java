package com.mangakousei.mangakousei_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalListRes {
    private Long proposalId;
    private String workingTitle;
    private String synopsis;
    private String targetAudience;
    private String status;
    private LocalDateTime createdAt;
    private String nameSummary;
    private String rejectionReason;
    private String revisionFeedback;
    private String sketchImageUrl;
    private MangakaInfo mangaka;
    private List<GenreInfo> genres;
    private List<CharacterInfo> characters;

    @Data
    public static class MangakaInfo {
        private Long userId;
        private String fullName;
        private String avatarUrl;
    }

    @Data
    public static class GenreInfo {
        @JsonProperty("genre_id")
        private Long genreId;
        private String name;
    }

    @Data
    public static class CharacterInfo {
        @JsonProperty("character_id")
        private Long characterId;

        @JsonProperty("character_name")
        private String characterName;

        private String role;
        private String description;
    }
}
