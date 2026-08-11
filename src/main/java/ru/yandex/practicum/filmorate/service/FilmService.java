package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.FilmResponse;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.InvalidParameterException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.dto.FilmRequest;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("genreDbStorage") GenreStorage genreStorage,
                       @Qualifier("mpaDbStorage") MpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Film convertToFilmForCreate(FilmRequest request) {
        log.info("Конвертация FilmRequest в Film для создания");

        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        // MPA
        if (request.getMpa() != null && request.getMpa().getId() != null) {
            Integer mpaId = request.getMpa().getId();
            try {
                MpaRating mpaRating = MpaRating.fromId(mpaId);
                film.setMpaRating(mpaRating);
            } catch (InvalidParameterException e) {
                throw new NotFoundException("Рейтинг MPA с ID " + mpaId + " не найден");
            }
        }

        // Жанры
        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            List<Genre> genres = new ArrayList<>();
            for (FilmRequest.GenreReference ref : request.getGenres()) {
                if (ref.getId() != null) {
                    try {
                        Genre genre = Genre.fromId(ref.getId());
                        genres.add(genre);
                    } catch (InvalidParameterException e) {
                        throw new NotFoundException("Жанр с ID " + ref.getId() + " не найден");
                    }
                }
            }
            film.setGenres(genres);
        } else {
            film.setGenres(new ArrayList<>());
        }

        if (film.getFilmUserLikes() == null) {
            film.setFilmUserLikes(new HashSet<>());
        }

        return film;
    }

    // Для обновления
    public Film convertToFilmForUpdate(FilmRequest request) {
        log.info("Конвертация FilmRequest в Film для ОБНОВЛЕНИЯ с ID: {}", request.getId());

        // Проверяем, что ID есть
        if (request.getId() == null) {
            throw new ConditionsNotMetException("ID фильма обязателен для обновления");
        }

        Film existingFilm = filmStorage.getFilmById(request.getId());
        log.info("Найден существующий фильм: {}", existingFilm);

        if (request.getName() != null) {
            existingFilm.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingFilm.setDescription(request.getDescription());
        }
        if (request.getReleaseDate() != null) {
            existingFilm.setReleaseDate(request.getReleaseDate());
        }
        if (request.getDuration() != null) {
            existingFilm.setDuration(request.getDuration());
        }
        if (request.getMpa() != null && request.getMpa().getId() != null) {
            setMpaRating(existingFilm, request.getMpa());
        }
        if (request.getGenres() != null) {
            setGenres(existingFilm, request.getGenres());
        }

        log.info("Фильм обновлен: {}", existingFilm);
        return existingFilm;
    }

    private void setMpaRating(Film film, FilmRequest.MpaReference mpaRef) {
        if (mpaRef != null && mpaRef.getId() != null) {
            Integer mpaId = mpaRef.getId();
            try {
                MpaRating mpaRating = MpaRating.fromId(mpaId);
                film.setMpaRating(mpaRating);
            } catch (InvalidParameterException e) {
                throw new NotFoundException("Рейтинг MPA с ID " + mpaId + " не найден");
            }
        }
    }

    private void setGenres(Film film, List<FilmRequest.GenreReference> genreRefs) {
        if (genreRefs != null && !genreRefs.isEmpty()) {
            List<Genre> genres = new ArrayList<>();
            for (FilmRequest.GenreReference ref : genreRefs) {
                if (ref.getId() != null) {
                    try {
                        Genre genre = Genre.fromId(ref.getId());
                        genres.add(genre);
                    } catch (InvalidParameterException e) {
                        throw new NotFoundException("Жанр с ID " + ref.getId() + " не найден");
                    }
                }
            }
            film.setGenres(genres);
        } else {
            film.setGenres(new ArrayList<>());
        }
    }

    public FilmResponse convertToResponse(Film film) {
        if (film == null) {
            return null;
        }

        FilmResponse.FilmResponseBuilder builder = FilmResponse.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration());

        // MPA
        if (film.getMpaRating() != null) {
            builder.mpa(FilmResponse.MpaResponse.builder()
                    .id(film.getMpaRating().getId())
                    .name(film.getMpaRating().getName())
                    .build());
        }

        // Жанры - ВСЕГДА возвращаем массив (даже пустой)
        List<FilmResponse.GenreResponse> genreResponses = new ArrayList<>();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreResponses = film.getGenres().stream()
                    .map(genre -> FilmResponse.GenreResponse.builder()
                            .id(genre.getId())
                            .name(genre.getName())
                            .build())
                    .collect(Collectors.toList());
        }
        builder.genres(genreResponses);  // ← ВСЕГДА УСТАНАВЛИВАЕМ

        return builder.build();
    }

    @Transactional
    public Film addFilm(Film film) {
        log.info("Сервис: создание фильма {}", film.getName());
        return filmStorage.addFilm(film);
    }

    @Transactional
    public Film updateFilm(Film film) {
        log.info("Сервис: обновление фильма {}", film.getName());

        if (film.getId() == null) {
            throw new ConditionsNotMetException("ID фильма обязателен для обновления");
        }

        filmStorage.getFilmById(film.getId());

        // Проверяем жанры, если они переданы
        if (film.getGenres() != null) {
            if (film.getGenres().isEmpty()) {
                film.setGenres(new ArrayList<>());
            } else {
                List<Genre> validGenres = new ArrayList<>();
                for (Genre genre : film.getGenres()) {
                    try {
                        Genre existing = Genre.fromId(genre.getId());
                        validGenres.add(existing);
                    } catch (IllegalArgumentException e) {
                        throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
                    }
                }
                film.setGenres(validGenres);
            }
        }

        return filmStorage.updateFilm(film);
    }

    public boolean deleteFilm(Long filmId) {
        log.info("Сервис: удаление фильма {}", filmId);
        return filmStorage.deleteFilm(filmId);
    }

    public Film getFilmById(Long filmId) {
        log.info("Сервис: получение фильма по ID {}", filmId);
        return filmStorage.getFilmById(filmId);
    }

    public Collection<Film> getAllFilms() {
        log.info("Сервис: получение всех фильмов");
        return filmStorage.getAllFilms();
    }

    public boolean addLike(Long userId, Long filmId) {
        userStorage.findUserById(userId);
        Film film = filmStorage.getFilmById(filmId);

        film.getFilmUserLikes().add(userId);
        filmStorage.updateFilm(film);

        log.info("Лайк добавлен пользователем {} к фильму {}", userId, filmId);
        return true;
    }

    public boolean deleteLike(Long userId, Long filmId) {
        userStorage.findUserById(userId);
        Film film = filmStorage.getFilmById(filmId);

        if (film.getFilmUserLikes().isEmpty() || !film.getFilmUserLikes().contains(userId)) {
            log.error("Попытка удалить несуществующий лайк от пользователя {} к фильму {}", userId, filmId);
            throw new NotFoundException("Попытка удалить несуществующий лайк от пользователя " + userId + " к фильму " + filmId);
        }

        film.getFilmUserLikes().remove(userId);
        filmStorage.syncLikes(film);

        log.info("Лайк удален пользователем {} к фильму {}", userId, filmId);
        return true;
    }

    public Collection<Film> getPopularFilms(int count) {
        log.info("FilmService: получение {} популярных фильмов", count);

        // Проверяем, что count положительный
        if (count <= 0) {
            log.warn("Запрошено неположительное количество фильмов: {}", count);
            return new HashSet<>();
        }

        return filmStorage.getPopularFilms(count);
    }
}
