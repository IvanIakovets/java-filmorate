package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

//методы добавления, удаления и модификации объектов.
public interface FilmStorage {

    Film addFilm(Film film);

    boolean deleteFilm(Long filmId);

    Film updateFilm(Film film);

    Collection<Film> getAllFilms();

    Film getFilmById(Long filmId);
}
