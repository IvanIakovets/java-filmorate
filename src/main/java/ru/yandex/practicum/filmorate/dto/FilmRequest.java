package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validations.ValidMpaRating;
import ru.yandex.practicum.filmorate.validations.ValidReleaseDate;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class FilmRequest {
    private Long id; // Для обновления

    @NotBlank(groups = ValidationGroups.Create.class)
    private String name;

    @Size(max = 200, groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private String description;

    @NotNull(groups = ValidationGroups.Create.class)
    @ValidReleaseDate(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    @JsonProperty("releaseDate")
    private LocalDate releaseDate;

    @Positive(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private Long duration;

    @NotNull(groups = ValidationGroups.Create.class)
    @ValidMpaRating(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private MpaReference mpa;

    private List<GenreReference> genres;

    @Data
    @NoArgsConstructor
    public static class MpaReference {
        private Integer id;
    }

    @Data
    @NoArgsConstructor
    public static class GenreReference {
        private Integer id;
    }
}