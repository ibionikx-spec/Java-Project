package com.mangakousei.mangakousei_backend.controller;

import com.mangakousei.mangakousei_backend.dto.response.ApiResponse;
import com.mangakousei.mangakousei_backend.dto.response.GenreRes;
import com.mangakousei.mangakousei_backend.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<GenreRes>>> getAllGenres() {
        List<GenreRes> genres = genreService.getAllGenres();
        return ResponseEntity.ok(ApiResponse.success("Fetched genres successfully", genres));
    }
}
