package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.DuplicateDataException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;

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

    @GetMapping("/{id}")
    public Film findFilmById(@PathVariable Long id) {
        log.info("Запрос на получение фильма по ID: {}", id);
        if (!films.containsKey(id)) {
            log.error("Фильм не найден. id: {}", id);
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        return films.get(id);
    }

    //публикация фильма
    @PostMapping
    public Film createFilm(@Validated(ValidationGroups.Create.class) @RequestBody Film film) {
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
    public Film updateFilm(@Validated(ValidationGroups.Update.class) @RequestBody Film film) {
        Long filmToChangeId = film.getId(); // ID - фильма который хотим изменить
        log.info("Запрос на изменение данных существующего фильма: id {} title {}",
                films.get(filmToChangeId).getName(), films.get(filmToChangeId).getName());
        /*if (filmToChangeId == null || filmToChangeId == 0) {
            log.error("Не указан id фильма");
            throw new ConditionsNotMetException("Id должен быть указан");
        }*/

        if (films.containsKey(filmToChangeId)) {
            for (Film fl : films.values()) {
                if (!fl.getId().equals(filmToChangeId) &&
                        fl.getName().equals(film.getName()) &&
                        fl.getReleaseDate().equals(film.getReleaseDate())) {
                    log.error("Попытка добавить дублирующий фильм");
                    throw new DuplicateDataException("Фильм с таким названием и датой релиза уже существует");
                }
            }
            Film oldFilm = films.get(filmToChangeId);
            log.info("Старт замены данных фильма");

            if (film.getName() != null) {
                oldFilm.setName(film.getName());
                log.info("Название фильма успешно изменено");
            }
            if (film.getDescription() != null) {
                oldFilm.setDescription(film.getDescription());
                log.info("Описание фильма успешно изменено");
            }
            if (film.getDuration() != null) {
                oldFilm.setDuration(film.getDuration());
                log.info("Продолжительность фильма успешно изменена");
            }
            if (film.getReleaseDate() != null) {
                oldFilm.setReleaseDate(film.getReleaseDate());
                log.info("Дата выхода фильма успешно изменена");
            }

            log.info("Данные фильма обновлены");

            return oldFilm;
        }
        log.error("Фильм по данному id {}, не обноружен", film.getId());
        throw new NotFoundException("Фильм с данным ID: " + film.getId() + "не обноружен");

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
