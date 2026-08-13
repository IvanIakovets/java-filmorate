package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreResponse;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public List<GenreResponse> getAllGenres() {
        log.info("GenreService: получение всех жанров");
        return genreStorage.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public GenreResponse getGenreById(Integer id) {
        log.info("GenreService: получение жанра по id {}", id);
        Genre genre = genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
        return convertToResponse(genre);
    }

    private GenreResponse convertToResponse(Genre genre) {
        if (genre == null) {
            return null;
        }
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
}