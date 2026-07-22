package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MpaRating {
    G("G", "Нет возрастных ограничений"),
    PG("PG", "Рекомендуется присутствие родителей"),
    PG_13("PG-13", "Детям до 13 лет просмотр не желателен"),
    R("R", "Лицам до 17 лет только в присутствии взрослого"),
    NC_17("NC-17", "Лицам до 18 лет просмотр запрещен");

    private final String code;
    private final String description;


    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static MpaRating fromString(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        // Проверяем по коду (G, PG, PG-13, R, NC-17)
        for (MpaRating rating : MpaRating.values()) {
            if (rating.code.equalsIgnoreCase(trimmed)) {
                return rating;
            }
        }

        // Проверяем по имени enum (G, PG, PG_13, R, NC_17)
        try {
            return MpaRating.valueOf(trimmed.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Указан неверный рейтинг MPA: '" + value +
                    "'. Поддерживаемые рейтинги: " + getAvailableRatings());
        }
    }

    private static String getAvailableRatings() {
        StringBuilder sb = new StringBuilder();
        for (MpaRating rating : MpaRating.values()) {
            sb.append("\n  - ").append(rating.code)
                    .append(" (").append(rating.description).append(")");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return code;
    }
}
