package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.UpdateSeriesReq;
import com.mangakousei.mangakousei_backend.dto.response.MangakaSeriesRes;
import com.mangakousei.mangakousei_backend.entity.entity.Chapter;
import com.mangakousei.mangakousei_backend.entity.entity.Genre;
import com.mangakousei.mangakousei_backend.entity.entity.Page;
import com.mangakousei.mangakousei_backend.entity.entity.PublicationSchedule;
import com.mangakousei.mangakousei_backend.entity.entity.Series;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class MangakaSeriesService {

    private final SeriesRepository seriesRepository;
    private final PublicationScheduleRepository scheduleRepository;
    private final GenreRepository genreRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterPageDeadlineRepository deadlineRepository;
    private final PageRepository pageRepository;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] downloadAllFiles(Long seriesId, Long mangakaId) {
        Series series = seriesRepository
                .findBySeriesIdAndCreatorUserId(seriesId, mangakaId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy series hoặc bạn không có quyền truy cập",
                        HttpStatus.NOT_FOUND));

        List<Chapter> chapters = chapterRepository
                .findBySeriesSeriesIdOrderByChapterNumberAsc(seriesId);

        if (chapters.isEmpty()) {
            throw new CustomAppException(
                    "Series chưa có chapter nào để tải", HttpStatus.BAD_REQUEST);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean hasAnyFile = false;

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Chapter chapter : chapters) {
                List<Page> pages = pageRepository
                        .findByChapterChapterIdOrderByPageNumberAsc(chapter.getChapterId());

                String folderName = "Chapter_" + chapter.getChapterNumber()
                        + (chapter.getTitle() != null && !chapter.getTitle().isBlank()
                            ? "_" + sanitizeFileName(chapter.getTitle())
                            : "");

                for (Page page : pages) {
                    String fileUrl = page.getFileUrl();
                    if (fileUrl == null || fileUrl.isBlank()) continue;

                    try {
                        byte[] imageBytes = downloadBytes(fileUrl);
                        String ext = extractExtension(fileUrl);
                        String entryName = folderName + "/page_"
                                + String.format("%03d", page.getPageNumber())
                                + "." + ext;

                        zos.putNextEntry(new ZipEntry(entryName));
                        zos.write(imageBytes);
                        zos.closeEntry();
                        hasAnyFile = true;
                    } catch (IOException e) {
                    }
                }
            }
        } catch (IOException e) {
            throw new CustomAppException(
                    "Lỗi khi tạo file zip", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!hasAnyFile) {
            throw new CustomAppException(
                    "Series chưa có ảnh trang nào để tải", HttpStatus.BAD_REQUEST);
        }

        return baos.toByteArray();
    }

    private byte[] downloadBytes(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream()) {
            return in.readAllBytes();
        }
    }

    private String extractExtension(String fileUrl) {
        String path = fileUrl.split("\\?")[0];
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx == -1 || dotIdx == path.length() - 1) return "jpg";
        String ext = path.substring(dotIdx + 1).toLowerCase();
        return ext.length() <= 5 ? ext : "jpg";
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    public List<MangakaSeriesRes> getSeriesByMangaka(Long mangakaId) {
        List<Series> seriesList =
                seriesRepository.findByCreatorUserIdOrderByApprovedAtDesc(mangakaId);

        return seriesList.stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    private MangakaSeriesRes toRes(Series s) {
        Optional<PublicationSchedule> schedule =
                scheduleRepository.findBySeriesSeriesId(s.getSeriesId());

        List<Long> chapterIds = chapterRepository
                .findBySeriesSeriesIdOrderByChapterNumberAsc(s.getSeriesId())
                .stream()
                .map(Chapter::getChapterId)
                .toList();

        long totalDeadlines = chapterIds.stream()
                .mapToLong(deadlineRepository::countByChapterChapterId)
                .sum();

        long submittedDeadlines = chapterIds.stream()
                .mapToLong(cid -> deadlineRepository.countByChapterChapterIdAndStatus(cid, "submitted"))
                .sum();

        return MangakaSeriesRes.builder()
                .seriesId(s.getSeriesId())
                .title(s.getTitle())
                .description(s.getDescription())
                .coverImageUrl(s.getCoverImageUrl())
                .seriesStatus(s.getSeriesStatus() != null
                        ? s.getSeriesStatus().getSeriesStatusName() : null)
                .tantouName(s.getEditor() != null
                        ? s.getEditor().getFullName() : null)
                .tantouAvatarUrl(s.getEditor() != null
                        ? s.getEditor().getAvatarUrl() : null)
                .chapterCount(s.getChapters() != null
                        ? s.getChapters().size() : 0)
                .genres(s.getGenres() != null
                        ? s.getGenres().stream()
                          .map(Genre::getGenreName)
                          .collect(Collectors.toList())
                        : List.of())
                .approvedAt(s.getApprovedAt() != null
                        ? s.getApprovedAt().format(DATE_FMT) : null)
                .scheduleType(schedule.map(PublicationSchedule::getScheduleType).orElse(null))
                .dayValue(schedule.map(PublicationSchedule::getDayValue).orElse(null))
                .totalPageDeadlines(totalDeadlines)
                .submittedPageDeadlines(submittedDeadlines)
                .build();
    }

    public MangakaSeriesRes getSeriesDetail(Long seriesId, Long mangakaId) {
        Series series = seriesRepository
                .findBySeriesIdAndCreatorUserId(seriesId, mangakaId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy series hoặc bạn không có quyền truy cập",
                        HttpStatus.NOT_FOUND));
        return toRes(series);
    }

    @Transactional
    public MangakaSeriesRes updateSeries(Long seriesId, Long mangakaId, UpdateSeriesReq req) {
        Series series = seriesRepository
                .findBySeriesIdAndCreatorUserId(seriesId, mangakaId)
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy series hoặc bạn không có quyền",
                        HttpStatus.NOT_FOUND));

        series.setTitle(req.getTitle());
        series.setDescription(req.getDescription());

        if (req.getCoverImageUrl() != null && !req.getCoverImageUrl().isBlank()) {
            series.setCoverImageUrl(req.getCoverImageUrl());
        }

        if (req.getGenreIds() != null) {
            List<Genre> genres = req.getGenreIds().stream()
                    .map(id -> genreRepository.findById(id)
                            .orElseThrow(() -> new CustomAppException(
                                    "Genre không tồn tại: " + id,
                                    HttpStatus.BAD_REQUEST)))
                    .collect(java.util.stream.Collectors.toList());
            series.setGenres(genres);
        }

        return toRes(seriesRepository.save(series));
    }
}