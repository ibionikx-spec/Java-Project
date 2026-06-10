package com.mangakousei.mangakousei_backend.service;

import com.mangakousei.mangakousei_backend.dto.response.GenreRes;
import com.mangakousei.mangakousei_backend.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public List<GenreRes> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(genre -> new GenreRes(genre.getGenreId(), genre.getGenreName()))
                .collect(Collectors.toList());
    }
}
