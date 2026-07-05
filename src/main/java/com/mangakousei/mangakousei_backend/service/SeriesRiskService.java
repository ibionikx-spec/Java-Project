package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.SeriesRiskRes;
import com.mangakousei.mangakousei_backend.entity.engagement.SeriesRanking;
import com.mangakousei.mangakousei_backend.repository.SeriesRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeriesRiskService {

    private static final BigDecimal LOW_SCORE_THRESHOLD = new BigDecimal("2.50");

    private final SeriesRankingRepository rankingRepository;

    @Transactional(readOnly = true)
    public List<SeriesRiskRes> getLatestRisks() {
        return rankingRepository.findLatestCalculatedAt()
                .map(this::evaluateSnapshot)
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<SeriesRiskRes> getLatestRisksForMangaka(Long mangakaId) {
        return getLatestRisks().stream()
                .filter(r -> belongsToMangaka(r.getSeriesId(), mangakaId))
                .toList();
    }

    public List<SeriesRiskRes> evaluateSnapshot(LocalDateTime calculatedAt) {
        List<SeriesRanking> ranking = rankingRepository.findByCalculatedAtOrderByRankingPositionAsc(calculatedAt);
        int total = ranking.size();
        if (total == 0) return List.of();

        return ranking.stream()
                .map(row -> toRisk(row, total))
                .filter(risk -> !"safe".equals(risk.getRiskLevel()))
                .toList();
    }

    private SeriesRiskRes toRisk(SeriesRanking row, int total) {
        SeriesRanking previous = rankingRepository
                .findTopBySeriesSeriesIdAndCalculatedAtBeforeOrderByCalculatedAtDesc(
                        row.getSeries().getSeriesId(),
                        row.getCalculatedAt())
                .orElse(null);

        int dangerStart = Math.max(1, (int) Math.ceil(total * 0.8));
        int watchStart = Math.max(1, (int) Math.ceil(total * 0.6));
        boolean lowScore = row.getSurveyScore() != null
                && row.getSurveyScore().compareTo(LOW_SCORE_THRESHOLD) < 0;
        boolean droppedHard = previous != null
                && row.getRankingPosition() - previous.getRankingPosition() >= 3;

        String riskLevel = "safe";
        String reason = "Series dang on dinh";

        if (row.getRankingPosition() >= dangerStart || lowScore) {
            riskLevel = "at_risk";
            reason = lowScore
                    ? "Diem khao sat duoi " + LOW_SCORE_THRESHOLD
                    : "Nam trong nhom 20% cuoi bang xep hang";
        } else if (row.getRankingPosition() >= watchStart || droppedHard) {
            riskLevel = "watch";
            reason = droppedHard
                    ? "Giam tu hang " + previous.getRankingPosition() + " xuong " + row.getRankingPosition()
                    : "Nam trong nhom can theo doi cua bang xep hang";
        }

        return SeriesRiskRes.builder()
                .seriesId(row.getSeries().getSeriesId())
                .title(row.getSeries().getTitle())
                .mangakaName(row.getSeries().getCreator() != null
                        ? row.getSeries().getCreator().getFullName() : null)
                .rankingPosition(row.getRankingPosition())
                .previousRankingPosition(previous != null ? previous.getRankingPosition() : null)
                .voteCount(row.getVoteCount())
                .riskLevel(riskLevel)
                .reason(reason)
                .build();
    }

    private boolean belongsToMangaka(Long seriesId, Long mangakaId) {
        return rankingRepository.findTopBySeriesSeriesIdOrderByCalculatedAtDesc(seriesId)
                .map(row -> row.getSeries().getCreator() != null
                        && row.getSeries().getCreator().getUserId().equals(mangakaId))
                .orElse(false);
    }
}
