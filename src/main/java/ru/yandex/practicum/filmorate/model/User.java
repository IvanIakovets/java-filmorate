package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    @NotNull(groups = {ValidationGroups.Update.class, ValidationGroups.Delete.class})
    private Long id;

    @NotBlank(message = "Email не может быть пустым",  groups = ValidationGroups.Create.class)
    @Email(message = "Email должен иметь корректный формат",
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private String email;

    @NotBlank(message = "Логин не может быть пустым",  groups = ValidationGroups.Create.class)
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы",
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private String login;

    @Size(max = 100, message = "Имя не должно превышать 100 символов",
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    private String name;

    @NotNull(message = "Дата рождения не может быть пустой", groups = ValidationGroups.Create.class)
    @Past(message = "Дата рождения должна быть в прошлом",
            groups = {ValidationGroups.Create.class, ValidationGroups.Update.class})
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private Set<Long> userFriends = new HashSet<>();
}
