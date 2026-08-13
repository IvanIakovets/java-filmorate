package ru.yandex.practicum.filmorate.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.dto.FilmRequest;

public class MpaRatingValidator implements ConstraintValidator<ValidMpaRating, FilmRequest.MpaReference> {

    @Override
    public boolean isValid(FilmRequest.MpaReference mpaRef, ConstraintValidatorContext context) {
        if (mpaRef == null || mpaRef.getId() == null) {
            return true;
        }

        // Проверяем, что ID положительный
        return mpaRef.getId() > 0;
    }
}