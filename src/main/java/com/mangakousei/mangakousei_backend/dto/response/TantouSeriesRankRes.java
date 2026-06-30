package com.mangakousei.mangakousei_backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TantouSeriesRankRes {
    private Long   seriesId;
    private String title;
    private String mangakaName;
    private Integer latestChapter;
    private String  latestChapterTitle;
    private long   voteCount;
    private double rating;
    private int    chapterCount;
}