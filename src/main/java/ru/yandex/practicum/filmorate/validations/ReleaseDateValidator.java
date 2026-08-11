package ru.yandex.practicum.filmorate.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<ValidReleaseDate, LocalDate> {
    private static final LocalDate minReleaseDate = LocalDate.of(1895, 12,28);

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        if (date == null) return true;
        return !date.isBefore(minReleaseDate);
    }
}
