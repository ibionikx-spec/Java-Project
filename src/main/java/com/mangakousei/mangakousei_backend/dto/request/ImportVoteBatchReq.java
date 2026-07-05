package com.mangakousei.mangakousei_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportVoteBatchReq {
    @NotBlank
    private String issueCodeName;

    @NotBlank
    private String note;

    @Valid
    @NotEmpty
    private List<VoteItem> votes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoteItem {
        @NotNull
        private Long seriesId;

        @NotNull
        @Min(0)
        private Long voteCount;

        @DecimalMin("0.0")
        @DecimalMax("5.0")
        private BigDecimal surveyScore;

        @Min(0)
        private Long salesCount;

        @Min(0)
        private Long commentCount;
    }
}
