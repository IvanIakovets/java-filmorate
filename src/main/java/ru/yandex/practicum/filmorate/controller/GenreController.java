package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.GenreResponse;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    // получть все жанры
    @GetMapping
    public List<GenreResponse> getAllGenres() {
        log.info("Запрос на получение всех жанров");
        return genreService.getAllGenres();
    }

    // полученье жанра по Id
    @GetMapping("/{id}")
    public GenreResponse getGenreById(@PathVariable Integer id) {
        log.info("Запрос на получение жанра по id {}", id);
        return genreService.getGenreById(id);
    }
}