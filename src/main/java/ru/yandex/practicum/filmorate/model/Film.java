package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.filmorate.validations.ValidReleaseDate;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Film {
    @NotNull(groups = {ValidationGroups.Update.class, ValidationGroups.Delete.class})
    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым", groups = ValidationGroups.Create.class)
    private String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов",
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private String description;

    @NotNull(message = "Дата релиза не может быть пустой", groups = ValidationGroups.Create.class)
    @ValidReleaseDate(groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом",
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private Long duration;

    private MpaRating mpaRating;

    private List<Genre> genres = new ArrayList<>();

    private Set<Long> filmUserLikes = new HashSet<>();
}
