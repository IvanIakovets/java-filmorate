package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

@Getter
@AllArgsConstructor
public enum MpaRating {
    G(1, "G"),
    PG(2, "PG"),
    PG_13(3, "PG-13"),
    R(4, "R"),
    NC_17(5, "NC-17");

    private final int id;
    private final String name;

    public static MpaRating fromId(Integer id) {
        if (id == null) {
            return null;
        }

        for (MpaRating rating : MpaRating.values()) {
            if (rating.getId() == id) {
                return rating;
            }
        }
        throw new NotFoundException("Рейтинг MPA с ID " + id + " не найден");
    }
}