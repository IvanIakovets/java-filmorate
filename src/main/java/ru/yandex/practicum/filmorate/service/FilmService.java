package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addFilm(Film film) {
        log.info("Сервис: создание фильма {}", film.getName());
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        log.info("Сервис: обновление фильма {}", film.getName());
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
        User user = userStorage.findUserById(userId);
        Film film = filmStorage.getFilmById(filmId);

        Set<Long> filmLikes = film.getFilmUserLikes();

        filmLikes.add(userId);
        log.info("Лайк добавлен пользователем {} к фильму {}", user.getId(), filmId);
        return true;
    }

    public boolean deleteLike(Long userId, Long filmId) {
        User user = userStorage.findUserById(userId);
        Film film = filmStorage.getFilmById(filmId);
        Set<Long> filmLikes = film.getFilmUserLikes();

        if (filmLikes.isEmpty() || !filmLikes.contains(userId)) {
            log.error("Попытка удалить несуществующий лайк от пользователя {} к фильму {}", userId, filmId);
            throw new NotFoundException("Попытка удалить несуществующий лайк от пользователя " + userId + " к фильму " + filmId);
        }

        filmLikes.remove(userId);
        log.info("Лайк удален пользователем {} к фильму {}", userId, filmId);
        return true;

    }

    public Collection<Film> getTenPopularFilms(int count) {
        Collection<Film> films = filmStorage.getAllFilms();

        if (films.isEmpty()) {
            log.error("Список фильмов пуст");
            return new HashSet<>();
        }

        return films.stream()
                .sorted((f1,f2) -> {
                    int likes1 = f1.getFilmUserLikes() != null ? f1.getFilmUserLikes().size() : 0;
                    int likes2 = f2.getFilmUserLikes() != null ? f2.getFilmUserLikes().size() : 0;
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }
}
