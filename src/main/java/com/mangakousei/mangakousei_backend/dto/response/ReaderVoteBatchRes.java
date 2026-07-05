package com.mangakousei.mangakousei_backend.dto.response;

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
public class ReaderVoteBatchRes {
    private Long batchId;
    private String issueCodeName;
    private String note;
    private LocalDateTime importedAt;
    private Long importedById;
    private String importedByName;
    private int voteItemCount;
    private List<SeriesRankingRes> ranking;
    private List<SeriesRiskRes> risks;
}
