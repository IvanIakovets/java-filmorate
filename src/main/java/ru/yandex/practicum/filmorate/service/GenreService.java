package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public List<Genre> getAllGenres() {
        log.info("GenreService: получение всех жанров");
        return genreStorage.findAll();
    }

    public Genre getGenreById(Integer id) {
        log.info("GenreService: получение жанра по id {}", id);
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
    }

    //Проверяет, что все жанры существуют, и возвращает их
    public Set<Genre> getGenresByIds(Set<Integer> genreIds) {
        log.info("GenreService: получение жанров по ID: {}", genreIds);

        if (genreIds == null || genreIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Genre> result = new HashSet<>();

        for (Integer id : genreIds) {
            if (id == null) {
                continue;
            }
            Genre genre = genreStorage.findById(id)
                    .orElseThrow(() -> {
                        log.error("Жанр с id {} не найден", id);
                        return new NotFoundException("Жанр с id " + id + " не найден");
                    });
            result.add(genre);
        }

        log.info("Найдено {} жанров", result.size());
        return result;
    }

    //Проверяет, что жанр с таким ID существует
    public boolean genreExists(Integer id) {
        if (id == null) {
            return false;
        }
        return genreStorage.findById(id).isPresent();
    }
}