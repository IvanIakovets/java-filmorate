package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmResponse;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;
import ru.yandex.practicum.filmorate.dto.FilmRequest;

import java.util.*;

@Slf4j
@RestController
@Validated
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    // публикация фильма
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmResponse createFilm(@Validated(ValidationGroups.Create.class) @RequestBody FilmRequest request) {
        log.info("Создание нового фильма: {}", request.getName());
        return filmService.addFilm(request);
    }

    // удаление фильма по Id
    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable("id") Long filmId) {
        log.info("Запрос на удаление фильма: {}", filmId);
        filmService.deleteFilm(filmId);
    }

    //изменение существующего фильма
    @PutMapping
    public FilmResponse updateFilm(@Validated(ValidationGroups.Update.class) @RequestBody FilmRequest request) {
        log.info("Обновление фильма с id: {}", request.getId());
        return filmService.updateFilm(request);
    }

    //получение списка всех фильмов
    @GetMapping
    public Collection<FilmResponse> findAllFilms() {
        return filmService.getAllFilms();
    }

    // найти фильм по Id
    @GetMapping("/{id}")
    public FilmResponse findFilmById(@PathVariable("id") Long filmId) {
        log.info("Запрос на получение фильма по ID: {}", filmId);
        return filmService.getFilmById(filmId);
    }

    // поставить лайк к фильму
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") Long filmId, @PathVariable Long userId) {
        log.info("Запрос на добавление лайка от пользователя {} к фильму {}", userId, filmId);
        filmService.addLike(userId, filmId);
    }

    // удалить лайк к фильму
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") Long filmId, @PathVariable Long userId) {
        log.info("Запрос на удаление лайка от пользователя {} у фильма {}", userId, filmId);
        filmService.deleteLike(userId, filmId);
    }

    // получить популярные фильмы
    @GetMapping("/popular")
    public Collection<FilmResponse> getPopularFilms(
            @Positive(message = "Количество фильмов должно быть положительным числом")
            @RequestParam(required = false, defaultValue = "10") Integer count) {
        log.info("Запрос на получение {} популярных фильмов", count);
        return filmService.getPopularFilms(count);
    }
}
