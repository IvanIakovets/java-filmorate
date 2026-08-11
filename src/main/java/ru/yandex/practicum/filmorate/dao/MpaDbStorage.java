package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    public MpaDbStorage(JdbcTemplate jdbcTemplate, MpaRowMapper mpaRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaRowMapper = mpaRowMapper;
    }

    @Override
    public List<MpaRating> findAll() {
        log.debug("MpaDbStorage: получение всех рейтингов");
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, mpaRowMapper);
    }

    @Override
    public Optional<MpaRating> findById(Integer id) {
        log.debug("MpaDbStorage: поиск рейтинга по id {}", id);
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        try {
            MpaRating mpa = jdbcTemplate.queryForObject(sql, mpaRowMapper, id);
            return Optional.of(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
