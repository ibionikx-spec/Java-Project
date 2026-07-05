package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class UpdateProposalReq {
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
    private List<CreateProposalReq.CharacterDto> characters;
}