package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmResponse;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;
import ru.yandex.practicum.filmorate.dto.FilmRequest;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Validated
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    //публикация фильма
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmResponse createFilm(@Validated(ValidationGroups.Create.class) @RequestBody FilmRequest request) {
        log.info("Создание нового фильма: {}", request.getName());

        Film film = filmService.convertToFilmForCreate(request);
        Film createdFilm = filmService.addFilm(film);
        return filmService.convertToResponse(createdFilm);
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable("id") Long filmId) {
        log.info("Запрос на удаление фильма: {}", filmId);
        filmService.deleteFilm(filmId);
    }

    //изменение существующего фильма
    @PutMapping
    public FilmResponse updateFilm(@Validated(ValidationGroups.Update.class) @RequestBody FilmRequest request) {
        log.info("Обновление фильма с id: {}", request.getId());

        if (request.getId() == null) {
            throw new ConditionsNotMetException("ID фильма обязателен для обновления");
        }

        Film film = filmService.convertToFilmForUpdate(request);
        Film updatedFilm = filmService.updateFilm(film);
        return filmService.convertToResponse(updatedFilm);
    }

    //получение списка всех фильмов.
    @GetMapping
    public Collection<FilmResponse> findAllFilms() { // ← было Collection<Film>
        Collection<Film> films = filmService.getAllFilms();
        return films.stream()
                .map(filmService::convertToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public FilmResponse findFilmById(@PathVariable("id") Long filmId) {
        log.info("Запрос на получение фильма по ID: {}", filmId);
        Film film = filmService.getFilmById(filmId);
        return filmService.convertToResponse(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") Long filmId, @PathVariable Long userId) {
        log.info("Запрос на добавление лайка от пользователя {} к фильму {}", userId, filmId);
        filmService.addLike(userId, filmId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") Long filmId, @PathVariable Long userId) {
        log.info("Запрос на удаление лайка от пользователя {} у фильма {}", userId, filmId);
        filmService.deleteLike(userId, filmId);
    }

    @GetMapping("/popular")
    public Collection<FilmResponse> getPopularFilms(
            @Positive(message = "Количество фильмов должно быть положительным числом")
            @RequestParam(required = false, defaultValue = "10") Integer count) {
        log.info("Запрос на получение {} популярных фильмов", count);
        return filmService.getPopularFilms(count).stream()
                .map(filmService::convertToResponse)
                .collect(Collectors.toList());
    }
}
