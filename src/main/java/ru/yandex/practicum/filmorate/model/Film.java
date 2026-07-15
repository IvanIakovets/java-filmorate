package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.filmorate.validations.ValidReleaseDate;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;

import java.time.LocalDate;

@Data
@Builder
public class Film {
    @NotNull(groups = ValidationGroups.Update.class)
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

    @PositiveOrZero(message = "Продолжительность фильма должна быть положительным числом",
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private Long duration;
}
