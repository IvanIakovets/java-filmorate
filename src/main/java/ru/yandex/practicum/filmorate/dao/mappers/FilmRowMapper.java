package ru.yandex.practicum.filmorate.dao.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FilmRowMapper implements RowMapper<Film> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        // обработка основных полей
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getLong("duration"));

        // обработка MPA рейтинга (проверки на корректность заполнения)
        try {
            Integer mpaId = rs.getObject("mpa_id", Integer.class);

            if (mpaId != null) {
                MpaRating mpaRating = MpaRating.fromId(mpaId);
                film.setMpaRating(mpaRating);
                log.debug("Установлен MPA: {} для фильма {}", mpaRating.getName(), film.getId());
            }
        } catch (SQLException e) {
            // Если колонки mpa_id нет в запросе - игнорируем
            log.debug("Колонка mpa_id не найдена в запросе, пропускаем MPA");
        } catch (NotFoundException e) {
            log.warn("MPA с id {} не найден", rs.getObject("mpa_id"));
            throw new NotFoundException("Рейтинг MPA с ID " + rs.getObject("mpa_id")  + " не найден");
        }

        // обработка жанров (проверки на корректность заполнения)
        try {
            String genresJson = rs.getString("genres");

            if (genresJson != null && !genresJson.equals("[]")
                    && !genresJson.equals("null")) {

                List<Genre> genres = parseGenresFromJson(genresJson);
                film.setGenres(genres);
                log.debug("Загружено {} жанров для фильма {}", genres.size(), film.getId());
            } else {
                // Если нет жанров - устанавливаем пустой список
                film.setGenres(new ArrayList<>());
                log.debug("Жанры отсутствуют для фильма {}", film.getId());
            }
        } catch (SQLException e) {
            // Если колонки genres нет в запросе - устанавливаем пустой список
            log.debug("Колонка 'genres' не найдена в запросе, устанавливаем пустой список");
            film.setGenres(new ArrayList<>());
        }

        return film;
    }

    // парсинг жанров
    private List<Genre> parseGenresFromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Genre>>() {});
        } catch (Exception e) {
            log.error("Ошибка парсинга жанров из JSON: {}", json, e);
            return new ArrayList<>();
        }
    }
}