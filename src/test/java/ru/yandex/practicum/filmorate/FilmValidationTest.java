package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование валидации модели Film")
class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Валидация поля name")
    class NameValidationTests {

        @Test
        @DisplayName("null name должен вызывать ошибку @NotBlank")
        void nullName_shouldFail() {
            Film film = createValidFilm();
            film.setName(null);

            Set<ConstraintViolation<Film>> violations = validator.validate(film, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Название фильма не может быть пустым");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Пустой или пробельный name должен вызывать ошибку")
        void emptyOrBlankName_shouldFail(String name) {
            Film film = createValidFilm();
            film.setName(name);

            Set<ConstraintViolation<Film>> violations = validator.validate(film, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Название фильма не может быть пустым");
        }

        @Test
        @DisplayName("Валидное название фильма должно проходить")
        void validName_shouldPass() {
            Film film = createValidFilm();
            film.setName("Valid Movie Name");

            Set<ConstraintViolation<Film>> violations = validator.validate(film);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Валидация поля description")
    class DescriptionValidationTests {

        @Test
        @DisplayName("null description должен проходить (поле опционально)")
        void nullDescription_shouldPass() {
            Film film = createValidFilm();
            film.setDescription(null);

            Set<ConstraintViolation<Film>> violations = validator.validate(film);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Пустой description должен проходить")
        void emptyDescription_shouldPass() {
            Film film = createValidFilm();
            film.setDescription("");

            Set<ConstraintViolation<Film>> violations = validator.validate(film);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Description длиной ровно 200 символов должен проходить")
        void descriptionLengthExactly200_shouldPass() {
            Film film = createValidFilm();
            film.setDescription("a".repeat(200));

            Set<ConstraintViolation<Film>> violations = validator.validate(film);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Description длиной 201 символ должен вызывать ошибку")
        void descriptionLength201_shouldFail() {
            Film film = createValidFilm();
            film.setDescription("a".repeat(201));

            Set<ConstraintViolation<Film>> violations = validator.validate(film, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Описание не может быть длиннее 200 символов");
        }
    }

    @Nested
    @DisplayName("Валидация поля releaseDate (кастомный валидатор)")
    class ReleaseDateValidationTests {

        @Test
        @DisplayName("null releaseDate должен вызывать ошибку @NotNull")
        void nullReleaseDate_shouldFail() {
            Film film = createValidFilm();
            film.setReleaseDate(null);

            Set<ConstraintViolation<Film>> violations = validator.validate(film, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Дата релиза не может быть пустой");
        }

        @Test
        @DisplayName("Дата релиза 28 декабря 1895 года должна проходить (граница)")
        void releaseDateAtMinimum_shouldPass() {
            Film film = createValidFilm();
            film.setReleaseDate(LocalDate.of(1895, 12, 28));

            Set<ConstraintViolation<Film>> violations = validator.validate(film);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Дата релиза 27 декабря 1895 года должна вызывать ошибку")
        void releaseDateBeforeMinimum_shouldFail() {
            Film film = createValidFilm();
            film.setReleaseDate(LocalDate.of(1895, 12, 27));

            Set<ConstraintViolation<Film>> violations = validator.validate(film, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        @Test
        @DisplayName("Очень старая дата (1800 год) должна вызывать ошибку")
        void veryOldReleaseDate_shouldFail() {
            Film film = createValidFilm();
            film.setReleaseDate(LocalDate.of(1800, 1, 1));

            Set<ConstraintViolation<Film>> violations = validator.validate(film, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        @Test
        @DisplayName("Дата релиза сегодня должна проходить")
        void todayReleaseDate_shouldPass() {
            Film film = createValidFilm();
            film.setReleaseDate(LocalDate.now());

            Set<ConstraintViolation<Film>> violations = validator.validate(film, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Дата релиза в будущем должна проходить (ограничений нет)")
        void futureReleaseDate_shouldPass() {
            Film film = createValidFilm();
            film.setReleaseDate(LocalDate.now().plusDays(365));

            Set<ConstraintViolation<Film>> violations = validator.validate(film);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Валидация поля duration")
    class DurationValidationTests {

        @Test
        @DisplayName("null duration должен проходить (поле опционально)")
        void nullDuration_shouldPass() {
            Film film = createValidFilm();
            film.setDuration(null);

            Set<ConstraintViolation<Film>> violations = validator.validate(film);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Продолжительность 0 минут должна проходить (@PositiveOrZero)")
        void zeroDuration_shouldPass() {
            Film film = createValidFilm();
            film.setDuration(0L);

            Set<ConstraintViolation<Film>> violations = validator.validate(film);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Продолжительность 1 минута должна проходить")
        void positiveDuration_shouldPass() {
            Film film = createValidFilm();
            film.setDuration(1L);

            Set<ConstraintViolation<Film>> violations = validator.validate(film);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Продолжительность Long.MAX_VALUE должна проходить")
        void maxDuration_shouldPass() {
            Film film = createValidFilm();
            film.setDuration(Long.MAX_VALUE);

            Set<ConstraintViolation<Film>> violations = validator.validate(film);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Отрицательная продолжительность должна вызывать ошибку")
        void negativeDuration_shouldFail() {
            Film film = createValidFilm();
            film.setDuration(-1L);

            Set<ConstraintViolation<Film>> violations = validator.validate(film, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Продолжительность фильма должна быть положительным числом");
        }

        @Test
        @DisplayName("Большой положительный размер должен проходить")
        void largePositiveDuration_shouldPass() {
            Film film = createValidFilm();
            film.setDuration(1_000_000_000L);

            Set<ConstraintViolation<Film>> violations = validator.validate(film);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Комплексная валидация Film")
    class FullValidationTests {

        @Test
        @DisplayName("Полностью валидный фильм должен проходить")
        void fullyValidFilm_shouldPass() {
            Film film = createValidFilm();

            Set<ConstraintViolation<Film>> violations = validator.validate(film);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Фильм со всеми невалидными полями должен возвращать все ошибки")
        void allInvalidFields_shouldReturnAllErrors() {
            Film film = Film.builder()
                    .name("")
                    .description("a".repeat(201))
                    .releaseDate(LocalDate.of(1895, 12, 27))
                    .duration(-1L)
                    .build();

            Set<ConstraintViolation<Film>> violations = validator.validate(film, ValidationGroups.Create.class);

            assertThat(violations).hasSize(4);

            assertThat(violations)
                    .extracting(ConstraintViolation::getPropertyPath)
                    .extracting(path -> path.toString())
                    .containsExactlyInAnyOrder("name", "description", "releaseDate", "duration");
        }
    }

    private Film createValidFilm() {
        return Film.builder()
                .name("Valid Movie")
                .description("Valid description")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(120L)
                .build();
    }
}
