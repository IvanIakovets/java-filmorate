package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.DuplicateDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Qualifier("memory")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        checkDuplicateFilm(film);
        film.setId(getNextId());
        log.info("Фильму выдан id: {}", film.getId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public boolean deleteFilm(Long filmId) {
        if (films.containsKey(filmId)) {
            films.remove(filmId);
            return true;
        } else {
            log.error("Фильм не найден. id: {}", filmId);
            throw new NotFoundException("Фильм с данным ID: " + filmId + " не найден");
        }
    }

    @Override
    public Film updateFilm(Film film) {
        Long filmToChangeId = film.getId(); // ID - фильма который хотим изменить
        log.info("Запрос на изменение данных существующего фильма: id {}", filmToChangeId);

        if (!films.containsKey(filmToChangeId)) {
            log.error("Фильм по данному id {}, не найден", film.getId());
            throw new NotFoundException("Фильм с данным ID: " + film.getId() + "не найден");

        }

        checkDuplicateFilm(film);
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
        if (film.getGenres() != null) {
            oldFilm.setGenres(film.getGenres());
            log.info("Жанр фильма успешно изменен на: {}", film.getGenres());
        }
        if (film.getMpaRating() != null) {
            oldFilm.setMpaRating(film.getMpaRating());
            log.info("Рейтинг MPA фильма успешно изменен на: {}", film.getMpaRating().getName());
        }

        log.info("Данные фильма обновлены");
        return oldFilm;
    }

    @Override
    public Collection<Film> getAllFilms() {
        if (films.isEmpty()) {
            log.error("Фильмы не найдены.");
            return new HashSet<>();
        }
        return films.values();
    }

    @Override
    public Film getFilmById(Long filmId) {
        if (!films.containsKey(filmId)) {
            log.error("Фильм не найден. id: {}", filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        return films.get(filmId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> {
                    int likes1 = f1.getFilmUserLikes() != null ? f1.getFilmUserLikes().size() : 0;
                    int likes2 = f2.getFilmUserLikes() != null ? f2.getFilmUserLikes().size() : 0;
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    private void checkDuplicateFilm(Film film) {
        for (Film fl : films.values()) {
            if (!Objects.equals(fl.getId(), film.getId()) && fl.getName().equals(film.getName()) &&
                    fl.getReleaseDate().equals(film.getReleaseDate())) {
                log.error("Попытка добавить дублирующий фильм");
                throw new DuplicateDataException("Фильм с таким названием и датой релиза уже существует");
            }
        }
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
