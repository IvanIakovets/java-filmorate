package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.GenreResponse;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public List<GenreResponse> getAllGenres() {
        log.info("Запрос на получение всех жанров");
        return genreService.getAllGenres().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public GenreResponse getGenreById(@PathVariable Integer id) {
        log.info("Запрос на получение жанра по id {}", id);
        Genre genre = genreService.getGenreById(id);
        return convertToResponse(genre);
    }

    private GenreResponse convertToResponse(Genre genre) {
        if (genre == null) {
            return null;
        }
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
}