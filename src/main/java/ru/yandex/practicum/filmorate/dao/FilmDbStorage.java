package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exceptions.DuplicateDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
@Qualifier("db")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    public final GenreRowMapper genreRowMapper;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper, GenreRowMapper genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
    }

    @Transactional
    @Override
    public Film addFilm(Film film) {
        log.info("FilmDbStorage: добавление фильма {}", film.getName());

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                ps.setLong(4, film.getDuration());

                if (film.getMpaRating() != null) {
                    ps.setInt(5, film.getMpaRating().getId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }

                return ps;
            }, keyHolder);
        } catch (DuplicateKeyException e) {
            log.error("Ошибка дубликата при добавлении фильма: {}", e.getMessage());
            throw new DuplicateDataException("Фильм с таким названием уже существует");
        }

        Long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);
        log.info("Сгенерирован ID: {}", filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenresToFilm(filmId, film.getGenres());
        } else {
            film.setGenres(new ArrayList<>());
        }
        return film;
    }

    @Override
    public void deleteFilm(Long filmId) {
        log.info("FilmDbStorage: удаление фильма {}", filmId);

        getFilmById(filmId);

        String sql = "DELETE FROM films WHERE id = ?";
        int rows = jdbcTemplate.update(sql, filmId);

        if (rows == 0) {
            log.error("Фильм с id {} не найден для удаления", filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        log.info("Фильм {} успешно удален", filmId);
    }

    @Transactional
    @Override
    public Film updateFilm(Film film) {
        log.info("FilmDbStorage: обновление фильма с id {}", film.getId());

        // Проверяем существование фильма
        getFilmById(film.getId());

        // Обновляем основные данные
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_rating_id = ? WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpaRating() != null ? film.getMpaRating().getId() : null,
                film.getId()
        );

        if (rowsAffected == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        // Обновляем жанры
        if (film.getGenres() != null) {
            addGenresToFilm(film.getId(), film.getGenres());
        }

        log.info("Фильм с id {} обновлен", film.getId());

        // Возвращаем обновленный фильм
        return film;
    }

    @Override
    public Collection<Film> getAllFilms() {
        log.info("FilmDbStorage: получение всех фильмов");

        String sql = "SELECT f.*, mr.id as mpa_id, mr.name as mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id " +
                "ORDER BY f.id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        loadGenresForFilms(films);

        log.info("Найдено {} фильмов", films.size());
        return films;
    }

    @Override
    public Film getFilmById(Long filmId) {
        log.info("FilmDbStorage: получение фильма по id {}", filmId);

        String sql = "SELECT f.*, mr.id as mpa_id, mr.name as mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id " +
                "WHERE f.id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, filmId);

            loadGenresForFilms(List.of(film));
            return film;
        } catch (EmptyResultDataAccessException e) {
            log.error("Фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        log.info("FilmDbStorage: получение {} популярных фильмов", count);

        String sql = "SELECT f.*, mr.id as mpa_id, mr.name as mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id " +
                "INNER JOIN ( " +
                "    SELECT film_id, COUNT(user_id) AS likes_count " +
                "    FROM film_likes " +
                "    GROUP BY film_id " +
                ") fl ON f.id = fl.film_id " +
                "ORDER BY fl.likes_count DESC, f.id ASC " +
                "LIMIT ?";

        try {
            List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);
            loadGenresForFilms(films);
            return films;
        } catch (Exception e) {
            log.error("Ошибка при получении популярных фильмов", e);
            return new ArrayList<>();
        }
    }

    private void addGenresToFilm(Long filmId, List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        // Превращаем в Set для удаления дубликатов
        Set<Genre> uniqueGenres = new HashSet<>(genres);

        // Удаляем старые жанры
        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);

        // Добавляем новые жанры
        String insertSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

        List<Object[]> batchArgs = uniqueGenres.stream()
                .map(genre -> new Object[]{filmId, genre.getId()})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(insertSql, batchArgs);
        log.info("Добавлено {} уникальных жанров для фильма {}", batchArgs.size(), filmId);
    }

    @Transactional
    public void addLike(Long filmId, Long userId) {
        log.info("FilmDbStorage: добавление лайка фильму {} от пользователя {}", filmId, userId);

        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, filmId, userId);
            log.info("Лайк успешно добавлен");
        } catch (DuplicateKeyException e) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            throw new DuplicateDataException("Пользователь уже поставил лайк этому фильму");
        }
    }

    @Transactional
    public void deleteLike(Long filmId, Long userId) {
        log.info("FilmDbStorage: удаление лайка у фильма {} от пользователя {}", filmId, userId);

        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, filmId, userId);

        if (rowsAffected == 0) {
            log.warn("Лайк от пользователя {} к фильму {} не найден", userId, filmId);
            throw new NotFoundException("Лайк не найден");
        }

        log.info("Лайк успешно удален");
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        // Получаем ID всех фильмов
        String filmIds = films.stream()
                .map(Film::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // Один запрос для получения жанров всех фильмов
        String genresSql = "SELECT fg.film_id, g.id, g.name " +
                "FROM film_genre fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + filmIds + ") " +
                "ORDER BY fg.film_id, g.id";

        // Группируем жанры по film_id
        Map<Long, List<Genre>> filmGenresMap = jdbcTemplate.query(genresSql, rs -> {
            Map<Long, List<Genre>> map = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Genre genre = genreRowMapper.mapRow(rs, 0);
                map.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
            }
            return map;
        });

        // Устанавливаем жанры для каждого фильма
        films.forEach(film -> {
            List<Genre> genres = filmGenresMap.getOrDefault(film.getId(), new ArrayList<>());
            film.setGenres(genres);
        });
    }
}