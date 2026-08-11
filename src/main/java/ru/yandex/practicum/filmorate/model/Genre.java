package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

@Getter
@AllArgsConstructor
public enum Genre {
    COMEDY(1, "Комедия"),
    DRAMA(2, "Драма"),
    ANIMATION(3, "Мультфильм"),
    THRILLER(4, "Триллер"),
    DOCUMENTARY(5, "Документальный"),
    ACTION(6, "Боевик");


    private final int id;
    private final String name;

    public static Genre fromId(Integer id) {
        if (id == null) {
            return null;
        }
        for (Genre genre : Genre.values()) {
            if (genre.id == id) {
                return genre;
            }
        }
        throw new NotFoundException("Жанр с ID " + id + " не найден");
    }
}