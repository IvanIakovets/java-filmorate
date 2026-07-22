package ru.yandex.practicum.filmorate.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MpaRatingValidator.class)
@Documented
@NotNull(message = "Рейтинг MPA не может быть пустым")
public @interface ValidMpaRating {
    String message() default "Указан неверный рейтинг MPA";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
