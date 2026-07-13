package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exeptions.DuplicateDataException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    //получение списка всех фильмов.
    @GetMapping
    public Collection<Film> findAllFilms() {
        log.info("Добавлен новый фильм. Всего фильмов {}", films.size());
        return films.values();
    }

    //публикация фильма
    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Создание нового фильма: {}", film.getName());
        for (Film fl : films.values()) {
            if (fl.getName().equals(film.getName()) &&
            fl.getDescription().equals(film.getDescription())) {
                log.error("Попытка добавить дублирующий фильм");
                throw new DuplicateDataException("Фильм с таким названием и датой релиза уже существует");
            }
        }

        film.setId(getNextId());
        log.info("Фильму выдан id: {}", film.getId());
        films.put(film.getId(), film);
        return film;
    }

    //изменение существующего фильма
    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Запрос на изменение данных существующего фильма: {}", newFilm.getId());
        if (newFilm.getId() == null) {
            log.error("Не указан id фильма");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (films.containsKey(newFilm.getId())) {
            for (Film fl : films.values()) {
                if (!fl.getId().equals(newFilm.getId()) &&
                        fl.getName().equals(newFilm.getName()) &&
                        fl.getReleaseDate().equals(newFilm.getReleaseDate())) {
                    log.error("Попытка добавить дублирующий фильм");
                    throw new DuplicateDataException("Фильм с таким названием и датой релиза уже существует");
                }
            }
            Film oldFilm = films.get(newFilm.getId());
            log.info("Старт замены данных фильма");

            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            log.info("Данные фильма успешно изменены");

            return oldFilm;
        }
        log.error("Фильм по данному id {}, не обноружен", newFilm.getId());
        throw new ConditionsNotMetException("Фильм с данным ID: " + newFilm.getId() + "не обноружен");

    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
