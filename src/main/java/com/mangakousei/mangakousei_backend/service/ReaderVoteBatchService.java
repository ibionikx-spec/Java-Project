package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.ImportVoteBatchReq;
import com.mangakousei.mangakousei_backend.dto.response.ReaderVoteBatchRes;
import com.mangakousei.mangakousei_backend.dto.response.SeriesRankingRes;
import com.mangakousei.mangakousei_backend.dto.response.SeriesRiskRes;
import com.mangakousei.mangakousei_backend.entity.engagement.ReaderVote;
import com.mangakousei.mangakousei_backend.entity.engagement.ReaderVoteBatches;
import com.mangakousei.mangakousei_backend.entity.engagement.SeriesRanking;
import com.mangakousei.mangakousei_backend.entity.entity.Series;
import com.mangakousei.mangakousei_backend.entity.entity.User;
import com.mangakousei.mangakousei_backend.entity.system.IssueCode;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.IssueCodeRepository;
import com.mangakousei.mangakousei_backend.repository.ReaderVoteBatchRepository;
import com.mangakousei.mangakousei_backend.repository.ReaderVoteRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesRankingRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesRepository;
import com.mangakousei.mangakousei_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReaderVoteBatchService {

    private final ReaderVoteBatchRepository batchRepository;
    private final ReaderVoteRepository voteRepository;
    private final IssueCodeRepository issueCodeRepository;
    private final SeriesRepository seriesRepository;
    private final SeriesRankingRepository rankingRepository;
    private final UserRepository userRepository;
    private final SeriesRiskService riskService;
    private final NotificationService notificationService;

    @Transactional
    public ReaderVoteBatchRes importBatch(ImportVoteBatchReq req, Long importerId) {
        User importer = userRepository.findById(importerId)
                .orElseThrow(() -> new CustomAppException("User not found", HttpStatus.NOT_FOUND));

        IssueCode issueCode = issueCodeRepository.findByIssueCodeName(req.getIssueCodeName())
                .orElseGet(() -> issueCodeRepository.save(IssueCode.builder()
                        .issueCodeName(req.getIssueCodeName())
                        .build()));

        ReaderVoteBatches batch = ReaderVoteBatches.builder()
                .issueCode(issueCode)
                .note(req.getNote())
                .importer(importer)
                .build();

        req.getVotes().forEach(item -> batch.getVotes().add(toVote(item, batch)));
        ReaderVoteBatches savedBatch = batchRepository.save(batch);

        List<SeriesRankingRes> ranking = recalculateRanking();
        List<SeriesRiskRes> risks = ranking.isEmpty()
                ? List.of()
                : riskService.evaluateSnapshot(ranking.getFirst().getCalculatedAt());
        notifyRisks(risks);

        return toBatchRes(savedBatch, ranking, risks);
    }

    @Transactional(readOnly = true)
    public List<ReaderVoteBatchRes> getRecentBatches() {
        return batchRepository.findTop20ByOrderByImportedAtDesc().stream()
                .map(batch -> toBatchRes(batch, List.of(), List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ReaderVoteBatchRes getBatch(Long batchId) {
        ReaderVoteBatches batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new CustomAppException("Khong tim thay batch", HttpStatus.NOT_FOUND));
        return toBatchRes(batch, getLatestRanking(), riskService.getLatestRisks());
    }

    @Transactional(readOnly = true)
    public List<SeriesRankingRes> getLatestRanking() {
        return rankingRepository.findLatestCalculatedAt()
                .map(time -> rankingRepository.findByCalculatedAtOrderByRankingPositionAsc(time)
                        .stream()
                        .map(this::toRankingRes)
                        .toList())
                .orElse(List.of());
    }

    private ReaderVote toVote(ImportVoteBatchReq.VoteItem item, ReaderVoteBatches batch) {
        Series series = seriesRepository.findById(item.getSeriesId())
                .orElseThrow(() -> new CustomAppException(
                        "Khong tim thay series: " + item.getSeriesId(), HttpStatus.BAD_REQUEST));

        ReaderVote vote = new ReaderVote();
        vote.setBatch(batch);
        vote.setSeries(series);
        vote.setVoteCount(item.getVoteCount());
        vote.setSurveyScore(item.getSurveyScore());
        vote.setSalesCount(item.getSalesCount() != null ? item.getSalesCount() : 0L);
        vote.setCommentCount(item.getCommentCount() != null ? item.getCommentCount() : 0L);
        return vote;
    }

    private List<SeriesRankingRes> recalculateRanking() {
        LocalDateTime calculatedAt = LocalDateTime.now();
        List<SeriesRanking> rows = seriesRepository.findAllPublicSeries().stream()
                .filter(s -> s.getSeriesStatus() == null
                        || !"cancelled".equalsIgnoreCase(s.getSeriesStatus().getSeriesStatusName()))
                .map(series -> toRankingRow(series, calculatedAt))
                .sorted(Comparator.comparing(SeriesRanking::getVoteCount).reversed()
                        .thenComparing(SeriesRanking::getSurveyScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(SeriesRanking::getSalesCount, Comparator.reverseOrder())
                        .thenComparing(SeriesRanking::getCommentCount, Comparator.reverseOrder()))
                .toList();

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setRankingPosition(i + 1);
        }
        return rankingRepository.saveAll(rows).stream().map(this::toRankingRes).toList();
    }

    private SeriesRanking toRankingRow(Series series, LocalDateTime calculatedAt) {
        SeriesRanking row = new SeriesRanking();
        row.setSeries(series);
        row.setVoteCount(voteRepository.sumVotesBySeriesId(series.getSeriesId()));
        row.setSurveyScore(BigDecimal.valueOf(voteRepository.averageScoreBySeriesId(series.getSeriesId())));
        row.setSalesCount(voteRepository.sumSalesBySeriesId(series.getSeriesId()));
        row.setCommentCount(voteRepository.sumCommentsBySeriesId(series.getSeriesId()));
        row.setCalculatedAt(calculatedAt);
        return row;
    }

    private SeriesRankingRes toRankingRes(SeriesRanking row) {
        SeriesRanking previous = rankingRepository
                .findTopBySeriesSeriesIdAndCalculatedAtBeforeOrderByCalculatedAtDesc(
                        row.getSeries().getSeriesId(),
                        row.getCalculatedAt())
                .orElse(null);

        return SeriesRankingRes.builder()
                .seriesId(row.getSeries().getSeriesId())
                .title(row.getSeries().getTitle())
                .mangakaName(row.getSeries().getCreator() != null
                        ? row.getSeries().getCreator().getFullName() : null)
                .rankingPosition(row.getRankingPosition())
                .previousRankingPosition(previous != null ? previous.getRankingPosition() : null)
                .voteCount(row.getVoteCount())
                .surveyScore(row.getSurveyScore())
                .salesCount(row.getSalesCount())
                .commentCount(row.getCommentCount())
                .calculatedAt(row.getCalculatedAt())
                .build();
    }

    private ReaderVoteBatchRes toBatchRes(
            ReaderVoteBatches batch,
            List<SeriesRankingRes> ranking,
            List<SeriesRiskRes> risks
    ) {
        return ReaderVoteBatchRes.builder()
                .batchId(batch.getBatchId())
                .issueCodeName(batch.getIssueCode() != null ? batch.getIssueCode().getIssueCodeName() : null)
                .note(batch.getNote())
                .importedAt(batch.getImportedAt())
                .importedById(batch.getImporter() != null ? batch.getImporter().getUserId() : null)
                .importedByName(batch.getImporter() != null ? batch.getImporter().getFullName() : null)
                .voteItemCount(batch.getVotes() != null ? batch.getVotes().size() : 0)
                .ranking(ranking)
                .risks(risks)
                .build();
    }

    private void notifyRisks(List<SeriesRiskRes> risks) {
        for (SeriesRiskRes risk : risks) {
            if (!"at_risk".equals(risk.getRiskLevel())) continue;

            seriesRepository.findById(risk.getSeriesId()).ifPresent(series -> {
                if (series.getCreator() != null) {
                    notificationService.send(series.getCreator().getUserId(), "SYSTEM",
                            "Series co nguy co bi huy",
                            series.getTitle() + " dang o vung nguy co: " + risk.getReason());
                }
                if (series.getEditor() != null) {
                    notificationService.send(series.getEditor().getUserId(), "SYSTEM",
                            "Series can ho so bao ve",
                            series.getTitle() + " dang o vung nguy co: " + risk.getReason());
                }
            });
        }
    }
}
