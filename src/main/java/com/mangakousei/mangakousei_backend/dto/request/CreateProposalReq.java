package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class CreateProposalReq {
    @NotBlank
    private String workingTitle;

    @NotBlank
    private String synopsis;

    @NotBlank
    private String targetAudience;

    private String nameSummary;
    private String sketchImageUrl;

    @NotNull
    @Size(min = 1, max = 5)
    private List<@NotNull Long> genreIds;

    @NotNull
    private List<CharacterDto> characters;

    @NotNull(message = "Vui lòng chọn Tantou phụ trách")
    private Long tantouId;

    @Data
    public static class CharacterDto {
        @NotBlank
        private String characterName;

        @NotBlank
        private String role;

        private String description;
    }
}