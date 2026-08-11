package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class FilmResponse {
    private Long id;
    private String name;
    private String description;

    @JsonProperty("releaseDate")
    private LocalDate releaseDate;

    private Long duration;

    @JsonProperty("mpa")
    private MpaResponse mpa;  // ← ТОЛЬКО ОДИН РАЗ!

    private List<GenreResponse> genres;

    @Data
    @Builder
    public static class MpaResponse {
        private Integer id;
        private String name;
    }

    @Data
    @Builder
    public static class GenreResponse {
        private Integer id;
        private String name;
    }
}