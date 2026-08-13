package ru.yandex.practicum.filmorate.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MpaRatingValidator.class)
@Documented
public @interface ValidMpaRating {
    String message() default "Указан неверный рейтинг MPA";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
