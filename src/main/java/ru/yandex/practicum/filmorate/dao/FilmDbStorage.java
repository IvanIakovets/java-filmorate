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
import ru.yandex.practicum.filmorate.exceptions.DuplicateDataException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
@Qualifier("db")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
    }

    @Transactional
    @Override
    public Film addFilm(Film film) {
        log.info("FilmDbStorage: добавление фильма {}", film.getName());

        checkDuplicateFilm(film);

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
                try {
                    ps.setInt(5, film.getMpaRating().getId());
                } catch (IllegalArgumentException e) {
                    throw new NotFoundException("Рейтинг MPA с ID " + film.getMpaRating().getId() + " не найден");
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

        syncGenres(film);
        syncLikes(film);

        log.info("Фильм создан с id {}", filmId);

        Film created = getFilmById(filmId);
        log.info("Возвращаемый фильм: {}", created);
        return created;
    }

    @Override
    public boolean deleteFilm(Long filmId) {
        log.info("FilmDbStorage: удаление фильма {}", filmId);

        getFilmById(filmId);

        String sql = "DELETE FROM films WHERE id = ?";
        int rows = jdbcTemplate.update(sql, filmId);

        if (rows == 0) {
            log.error("Фильм с id {} не найден для удаления", filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        log.info("Фильм {} успешно удален", filmId);
        return true;
    }

    @Transactional
    @Override
    public Film updateFilm(Film film) {
        log.info("FilmDbStorage: обновление фильма {}", film.getId());

        getFilmById(film.getId());
        checkDuplicateFilmForUpdate(film);

        List<String> updateFields = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (film.getName() != null) {
            updateFields.add("name = ?");
            params.add(film.getName());
        }
        if (film.getDescription() != null) {
            updateFields.add("description = ?");
            params.add(film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            updateFields.add("release_date = ?");
            params.add(Date.valueOf(film.getReleaseDate()));
        }
        if (film.getDuration() != null) {
            updateFields.add("duration = ?");
            params.add(film.getDuration());
        }
        if (film.getMpaRating() != null) {
            updateFields.add("mpa_rating_id = ?");
            params.add(film.getMpaRating().getId());
        }

        if (!updateFields.isEmpty()) {
            params.add(film.getId());
            String sql = "UPDATE films SET " + String.join(", ", updateFields) + " WHERE id = ?";
            jdbcTemplate.update(sql, params.toArray());
        }

        syncLikes(film);
        syncGenres(film);

        log.info("Фильм {} успешно обновлен", film.getId());
        return getFilmById(film.getId());
    }

    @Override
    public Collection<Film> getAllFilms() {
        log.info("FilmDbStorage: получение всех фильмов");

        String sql = "SELECT f.*, " +
                "mr.id as mpa_id, mr.name as mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        films.forEach(film -> {
            loadGenres(film);
            loadLikes(film);
        });

        log.info("Найдено {} фильмов", films.size());
        return films;
    }

    @Override
    public Film getFilmById(Long filmId) {
        log.info("FilmDbStorage: получение фильма по id {}", filmId);

        String sql = "SELECT f.*, " +
                "mr.id as mpa_id, mr.name as mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id " +
                "WHERE f.id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, filmId);
            loadGenres(film);
            loadLikes(film);
            return film;
        } catch (EmptyResultDataAccessException e) {
            log.error("Фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }

    @Transactional
    private void syncGenres(Film film) {
        if (film.getId() == null) {
            log.warn("Нельзя синхронизировать жанры для фильма без ID");
            return;
        }

        log.debug("Синхронизация жанров для фильма {}", film.getId());

        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String insertSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(insertSql, film.getId(), genre.getId());
            }
            log.debug("Добавлено {} связей жанров для фильма {}", film.getGenres().size(), film.getId());
        } else {
            log.debug("Жанры для фильма {} очищены", film.getId());
        }
    }

    private void loadGenres(Film film) {
        if (film.getId() == null) {
            film.setGenres(new ArrayList<>());
            return;
        }

        String sql = """
            SELECT g.id, g.name
            FROM film_genre fg
            JOIN genres g ON fg.genre_id = g.id
            WHERE fg.film_id = ?
            ORDER BY g.id
            """;

        try {
            List<Genre> genres = jdbcTemplate.query(sql,
                    (rs, rowNum) -> {
                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        log.info("Загружен жанр: id={}, name={}", id, name);  // ← ДОБАВЬ!
                        try {
                            return Genre.fromId(id);
                        } catch (Exception e) {
                            log.error("Ошибка при получении жанра с id={}: {}", id, e.getMessage());
                            return null;  // ← ВРЕМЕННО
                        }
                    },
                    film.getId()
            );

            // ✅ Убираем null значения
            List<Genre> filteredGenres = genres.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            film.setGenres(new ArrayList<>(filteredGenres));
            log.debug("Загружено {} жанров для фильма {}", filteredGenres.size(), film.getId());
        } catch (Exception e) {
            log.error("Ошибка загрузки жанров для фильма {}: {}", film.getId(), e.getMessage());
            film.setGenres(new ArrayList<>());
        }
    }

    private void loadLikes(Film film) {
        if (film.getId() == null) {
            film.setFilmUserLikes(new HashSet<>());
            return;
        }

        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        Set<Long> userIds = new HashSet<>(jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("user_id"),
                film.getId()
        ));
        film.setFilmUserLikes(userIds);
        log.debug("Загружено {} лайков для фильма {}", userIds.size(), film.getId());
    }

    @Transactional
    @Override
    public void syncLikes(Film film) {
        if (film.getId() == null) {
            log.warn("Нельзя синхронизировать лайки для фильма без ID");
            return;
        }

        log.debug("Синхронизация лайков для фильма {}", film.getId());

        String selectSql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        Set<Long> existingLikes = new HashSet<>(
                jdbcTemplate.query(selectSql,
                        (rs, rowNum) -> rs.getLong("user_id"),
                        film.getId())
        );

        Set<Long> newLikes = film.getFilmUserLikes();
        if (newLikes == null) {
            newLikes = new HashSet<>();
        }

        Set<Long> toDelete = new HashSet<>(existingLikes);
        toDelete.removeAll(newLikes);

        Set<Long> toAdd = new HashSet<>(newLikes);
        toAdd.removeAll(existingLikes);

        if (!toDelete.isEmpty()) {
            String deleteSql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
            for (Long userId : toDelete) {
                jdbcTemplate.update(deleteSql, film.getId(), userId);
            }
            log.debug("Удалено {} лайков для фильма {}", toDelete.size(), film.getId());
        }

        if (!toAdd.isEmpty()) {
            String insertSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            for (Long userId : toAdd) {
                jdbcTemplate.update(insertSql, film.getId(), userId);
            }
            log.debug("Добавлено {} лайков для фильма {}", toAdd.size(), film.getId());
        }

        film.setFilmUserLikes(newLikes);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        log.info("DbFilmStorage: получение {} популярных фильмов", count);

        String sql = "SELECT f.*, " +
                "mr.id as mpa_id, mr.name as mpa_name, " +  // ← запятая и пробел!
                "COUNT(fl.user_id) as likes_count " +        // ← пробел!
                "FROM films f " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id, mr.id, mr.name " +           // ← пробел!
                "ORDER BY likes_count DESC, f.id " +         // ← пробел!
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);

        films.forEach(this::loadGenres);
        films.forEach(this::loadLikes);

        log.info("Найдено {} популярных фильмов", films.size());
        return films;
    }

    private Integer getMpaIdById(String name) {
        String sql = "SELECT id FROM mpa_ratings WHERE name = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, name);
        } catch (EmptyResultDataAccessException e) {
            log.error("MPA рейтинг с кодом {} не найден", name);
            throw new NotFoundException("MPA рейтинг с кодом " + name + " не найден");
        }
    }

    private void checkDuplicateFilm(Film film) {
        String sql = "SELECT COUNT(*) FROM films WHERE name = ? AND release_date = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                film.getName(), Date.valueOf(film.getReleaseDate()));

        if (count != null && count > 0) {
            log.error("Фильм с названием {} и датой релиза {} уже существует",
                    film.getName(), film.getReleaseDate());
            throw new DuplicateDataException("Фильм с таким названием и датой релиза уже существует");
        }
    }

    private void checkDuplicateFilmForUpdate(Film film) {
        String sql = "SELECT COUNT(*) FROM films WHERE name = ? AND release_date = ? AND id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                film.getName(), Date.valueOf(film.getReleaseDate()), film.getId());

        if (count != null && count > 0) {
            log.error("Фильм с названием {} и датой релиза {} уже существует",
                    film.getName(), film.getReleaseDate());
            throw new DuplicateDataException("Фильм с таким названием и датой релиза уже существует");
        }
    }
}