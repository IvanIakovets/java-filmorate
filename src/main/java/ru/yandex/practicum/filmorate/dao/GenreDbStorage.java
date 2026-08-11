package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public GenreDbStorage(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public List<Genre> findAll() {
        log.debug("GenreDbStorage: получение всех жанров");
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, genreRowMapper);
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        log.debug("GenreDbStorage: поиск жанра по id {}", id);
        String sql = "SELECT * FROM genres WHERE id = ?";
        try {
            Genre genre = jdbcTemplate.queryForObject(sql, genreRowMapper, id);
            return Optional.of(genre);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Жанр с id {} не найден", id);
            return Optional.empty();
        }
    }
}