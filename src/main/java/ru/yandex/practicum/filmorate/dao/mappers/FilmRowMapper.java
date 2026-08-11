package ru.yandex.practicum.filmorate.dao.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.InvalidParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        // Основные поля
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        // Дата релиза
        Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }

        film.setDuration(rs.getLong("duration"));

        // MPA Rating (из JOIN с mpa_ratings)
        int mpaId = rs.getInt("mpa_id");
        if (mpaId != 0) {
            try {
                MpaRating mpaRating = MpaRating.fromId(mpaId);
                film.setMpaRating(mpaRating);
            } catch (InvalidParameterException e) {
                log.warn("Неизвестный MPA код: {}", mpaId, e);
            }
        }

        return film;
    }
}
