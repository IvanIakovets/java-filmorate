package ru.yandex.practicum.filmorate.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.exeptions.IllegalArgumentException;
import ru.yandex.practicum.filmorate.model.Genre;

public class GenreValidator implements ConstraintValidator<ValidGenre, Genre> {
    @Override
    public boolean isValid(Genre genre, ConstraintValidatorContext constraintValidatorContext) {
        if (genre == null) {
            return true; // Пропускаем null, так как есть @NotNull
        }

        // Проверяем, что жанр существует в enum
        try {
            Genre.valueOf(genre.name());
            return true;
        } catch (IllegalArgumentException e) {
            // Добавляем список доступных жанров в сообщение
            String availableGenres = getAvailableGenres();
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(
                    "Жанр '" + genre.getDisplayName() + "' не существует. Доступные жанры: " + availableGenres
            ).addConstraintViolation();
            return false;
        }
    }

    private String getAvailableGenres() {
        StringBuilder sb = new StringBuilder();
        for (Genre g : Genre.values()) {
            sb.append("\n  - ").append(g.getDisplayName())
                    .append(" (").append(g.name()).append(")");
        }
        return sb.toString();
    }
}
