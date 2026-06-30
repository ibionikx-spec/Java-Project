package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.request.ReaderInteractionReq;
import com.mangakousei.mangakousei_backend.dto.response.*;
import com.mangakousei.mangakousei_backend.service.PublicMangaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicMangaController {
    private final PublicMangaService publicMangaService;

    @GetMapping("/series")
    public ResponseEntity<?> getSeries() {
        return ResponseEntity.ok(ApiResponse.success("Fetched public series", publicMangaService.getSeries()));
    }

    @GetMapping("/series/{seriesId}")
    public ResponseEntity<?> getSeries(@PathVariable Long seriesId) {
        return ResponseEntity.ok(ApiResponse.success("Fetched series", publicMangaService.getSeries(seriesId)));
    }

    @GetMapping("/series/{seriesId}/chapters")
    public ResponseEntity<?> getChapters(@PathVariable Long seriesId) {
        return ResponseEntity.ok(ApiResponse.success("Fetched public chapters", publicMangaService.getChapters(seriesId)));
    }

    @GetMapping("/ranking")
    public ResponseEntity<?> getRanking() {
        return ResponseEntity.ok(ApiResponse.success("Fetched ranking", publicMangaService.getRanking()));
    }

    @PostMapping("/series/{seriesId}/vote")
    public ResponseEntity<?> vote(@PathVariable Long seriesId, @Valid @RequestBody ReaderInteractionReq request) {
        return ResponseEntity.ok(ApiResponse.success("Vote saved", publicMangaService.vote(seriesId, request)));
    }

    @PostMapping("/series/{seriesId}/rating")
    public ResponseEntity<?> rate(@PathVariable Long seriesId, @Valid @RequestBody ReaderInteractionReq request) {
        return ResponseEntity.ok(ApiResponse.success("Rating saved", publicMangaService.rate(seriesId, request)));
    }
}
