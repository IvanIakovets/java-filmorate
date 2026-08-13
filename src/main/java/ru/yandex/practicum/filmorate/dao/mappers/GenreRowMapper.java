package ru.yandex.practicum.filmorate.dao.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Component
public class GenreRowMapper implements RowMapper<Genre> {

    @Override
    public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");

        try {
            Genre genre = Genre.fromId(id);
            log.debug("Маппинг жанра: id={}, name={}, enum={}", id, name, genre);
            return genre;
        } catch (IllegalArgumentException e) {
            log.error("Неизвестный жанр с id {}: {}", id, name);
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
    }
}