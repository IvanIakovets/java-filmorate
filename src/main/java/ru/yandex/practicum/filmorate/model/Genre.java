package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import ru.yandex.practicum.filmorate.exeptions.IllegalArgumentException;

@AllArgsConstructor
public enum Genre {
    COMEDY("Комедия"),
    DRAMA("Драма"),
    SCIENCE_FICTION("Фантастика"),
    FANTASY("Фэнтези"),
    THRILLER("Триллер"),
    HORROR("Ужасы"),
    ROMANCE("Мелодрама"),
    ADVENTURE("Приключения"),
    ANIMATION("Мультфильм"),
    DOCUMENTARY("Документальный");

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static Genre fromString(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        for (Genre genre : Genre.values()) {
            if (genre.displayName.equalsIgnoreCase(trimmed)) {
                return genre;
            }
        }

        try {
            return Genre.valueOf(trimmed.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Указан неверный жанр: '" + value +
                    "'. Поддерживаемые жанры: " + getAvailableGenres());
        }
    }

    private static String getAvailableGenres() {
        StringBuilder sb = new StringBuilder();
        for (Genre genre : Genre.values()) {
            sb.append("\n  - ").append(genre.displayName)
                    .append(" (").append(genre.name()).append(")");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return displayName;
    }
}
