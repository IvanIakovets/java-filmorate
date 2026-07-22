package ru.yandex.practicum.filmorate.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.model.MpaRating;

public class MpaRatingValidator implements ConstraintValidator<ValidMpaRating, MpaRating> {
    @Override
    public boolean isValid(MpaRating rating, ConstraintValidatorContext constraintValidatorContext) {
        if (rating == null) {
            return true; // Пропускаем null, так как есть @NotNull
        }

        // Проверяем, что рейтинг существует в enum
        try {
            MpaRating.valueOf(rating.name());
            return true;
        } catch (IllegalArgumentException e) {
            String availableRatings = getAvailableRatings();
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "Рейтинг MPA '" + rating.getCode() + "' не существует. Доступные рейтинги: " + availableRatings
            ).addConstraintViolation();
            return false;
        }
    }

    private String getAvailableRatings() {
        StringBuilder sb = new StringBuilder();
        for (MpaRating rating : MpaRating.values()) {
            sb.append("\n  - ").append(rating.getCode())
                    .append(" (").append(rating.getDescription()).append(")");
        }
        return sb.toString();
    }
}
