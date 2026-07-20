package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;

import java.util.*;

@Slf4j
@RestController
@Validated
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    //публикация фильма
    @PostMapping
    public Film createFilm(@Validated(ValidationGroups.Create.class) @RequestBody Film film) {
        log.info("Создание нового фильма: {}", film.getName());
        return filmService.addFilm(film);
    }

    @DeleteMapping("/{id}")
    public boolean deleteFilmById(@PathVariable("id") Long filmId) {
        log.info("Запрос на удаление фильма: {}", filmId);
        return filmService.deleteFilm(filmId);
    }

    //изменение существующего фильма
    @PutMapping
    public Film updateFilm(@Validated(ValidationGroups.Update.class) @RequestBody Film film) {
        log.info("Запрос на обновление фильма: {}", film.getName());
        return filmService.updateFilm(film);
    }

    //получение списка всех фильмов.
    @GetMapping
    public Collection<Film> findAllFilms() {
        log.info("Получен запрос на отправку списка всех фильмов. Всего фильмов {}", filmService.getAllFilms().size());
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film findFilmById(@PathVariable("id") Long filmId) {
        log.info("Запрос на получение фильма по ID: {}", filmId);
        return filmService.getFilmById(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public boolean addLike(@PathVariable("id") Long filmId, @PathVariable Long userId) {
        log.info("Запрос на добавление лайка от пользователя {} к фильму {}", userId, filmId);
        return filmService.addLike(userId, filmId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public boolean deleteLike(@PathVariable("id") Long filmId, @PathVariable Long userId) {
        log.info("Запрос на удаление лайка от пользователя {} у фильма {}", userId, filmId);
        return filmService.deleteLike(userId, filmId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(
            @Positive(message = "Количество фильмов должно быть положительным числом")
            @RequestParam(required = false, defaultValue = "10") Integer count) {
        log.info("Запрос на получение {} популярных фильмов", count);
        return filmService.getTenPopularFilms(count);
    }
}
