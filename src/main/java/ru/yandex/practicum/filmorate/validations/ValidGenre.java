package ru.yandex.practicum.filmorate.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.*;

@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenreValidator.class)
@Documented
@NotNull(message = "Жанр не может быть пустым")
public @interface ValidGenre {
    String message() default "Указан неверный жанр";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
