package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.request.SetScheduleReq;
import com.mangakousei.mangakousei_backend.dto.response.ScheduleEntryRes;
import com.mangakousei.mangakousei_backend.entity.entity.PublicationSchedule;
import com.mangakousei.mangakousei_backend.entity.entity.Series;
import com.mangakousei.mangakousei_backend.exception.CustomAppException;
import com.mangakousei.mangakousei_backend.repository.PublicationScheduleRepository;
import com.mangakousei.mangakousei_backend.repository.SeriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicationScheduleService {

    private final PublicationScheduleRepository scheduleRepository;
    private final SeriesRepository seriesRepository;

    public List<ScheduleEntryRes> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleEntryRes setSchedule(SetScheduleReq req) {
        Series series = seriesRepository.findById(req.getSeriesId())
                .orElseThrow(() -> new CustomAppException(
                        "Không tìm thấy series", HttpStatus.NOT_FOUND));

        if (!List.of("weekly", "monthly").contains(req.getScheduleType())) {
            throw new CustomAppException(
                    "scheduleType phải là 'weekly' hoặc 'monthly'",
                    HttpStatus.BAD_REQUEST);
        }

        if ("weekly".equals(req.getScheduleType()) && req.getDayValue() > 7) {
            throw new CustomAppException(
                    "dayValue cho weekly phải từ 1 (T2) đến 7 (CN)",
                    HttpStatus.BAD_REQUEST);
        }

        PublicationSchedule schedule = scheduleRepository
                .findBySeriesSeriesId(req.getSeriesId())
                .orElse(PublicationSchedule.builder().series(series).build());

        schedule.setScheduleType(req.getScheduleType());
        schedule.setDayValue(req.getDayValue());

        return toRes(scheduleRepository.save(schedule));
    }

    public ScheduleEntryRes getScheduleBySeries(Long seriesId) {
        return scheduleRepository.findBySeriesSeriesId(seriesId)
                .map(this::toRes)
                .orElseThrow(() -> new CustomAppException(
                        "Series này chưa có lịch xuất bản", HttpStatus.NOT_FOUND));
    }

    private ScheduleEntryRes toRes(PublicationSchedule s) {
        return ScheduleEntryRes.builder()
                .scheduleId(s.getScheduleId())
                .seriesId(s.getSeries().getSeriesId())
                .seriesTitle(s.getSeries().getTitle())
                .mangakaName(s.getSeries().getCreator() != null
                        ? s.getSeries().getCreator().getFullName() : "")
                .scheduleType(s.getScheduleType())
                .dayValue(s.getDayValue())
                .build();
    }
}
