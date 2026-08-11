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
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.validations.ValidationGroups;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование валидации FilmRequest DTO")
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
        @DisplayName("null name должен вызывать ошибку")
        void nullName_shouldFail() {
            FilmRequest request = createValidRequest();
            request.setName(null);

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("не должно быть пустым") || msg.contains("must not be blank"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Пустой или пробельный name должен вызывать ошибку")
        void emptyOrBlankName_shouldFail(String name) {
            FilmRequest request = createValidRequest();
            request.setName(name);

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("не должно быть пустым") || msg.contains("must not be blank"));
        }

        @Test
        @DisplayName("Валидное название фильма должно проходить")
        void validName_shouldPass() {
            FilmRequest request = createValidRequest();
            request.setName("Valid Movie Name");

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Валидация поля description")
    class DescriptionValidationTests {

        @Test
        @DisplayName("null description должен проходить (поле опционально)")
        void nullDescription_shouldPass() {
            FilmRequest request = createValidRequest();
            request.setDescription(null);

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Пустой description должен проходить")
        void emptyDescription_shouldPass() {
            FilmRequest request = createValidRequest();
            request.setDescription("");

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Description длиной ровно 200 символов должен проходить")
        void descriptionLengthExactly200_shouldPass() {
            FilmRequest request = createValidRequest();
            request.setDescription("a".repeat(200));

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Description длиной 201 символ должен вызывать ошибку")
        void descriptionLength201_shouldFail() {
            FilmRequest request = createValidRequest();
            request.setDescription("a".repeat(201));

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("размер должен находиться в диапазоне от 0 до 200") ||
                            msg.contains("size must be between 0 and 200"));
        }
    }

    @Nested
    @DisplayName("Валидация поля releaseDate (кастомный валидатор)")
    class ReleaseDateValidationTests {

        @Test
        @DisplayName("Дата релиза 28 декабря 1895 года должна проходить (граница)")
        void releaseDateAtMinimum_shouldPass() {
            FilmRequest request = createValidRequest();
            request.setReleaseDate(LocalDate.of(1895, 12, 28));

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Дата релиза 27 декабря 1895 года должна вызывать ошибку")
        void releaseDateBeforeMinimum_shouldFail() {
            FilmRequest request = createValidRequest();
            request.setReleaseDate(LocalDate.of(1895, 12, 27));

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("Дата релиза не может быть раньше 28 декабря 1895 года"));
        }

        @Test
        @DisplayName("Очень старая дата (1800 год) должна вызывать ошибку")
        void veryOldReleaseDate_shouldFail() {
            FilmRequest request = createValidRequest();
            request.setReleaseDate(LocalDate.of(1800, 1, 1));

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("Дата релиза не может быть раньше 28 декабря 1895 года"));
        }

        @Test
        @DisplayName("Дата релиза в будущем должна проходить")
        void futureReleaseDate_shouldPass() {
            FilmRequest request = createValidRequest();
            request.setReleaseDate(LocalDate.now().plusDays(365));

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Валидация поля duration")
    class DurationValidationTests {

        @Test
        @DisplayName("null duration должен проходить (поле опционально)")
        void nullDuration_shouldPass() {
            FilmRequest request = createValidRequest();
            request.setDuration(null);

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Продолжительность 1 минута должна проходить")
        void positiveDuration_shouldPass() {
            FilmRequest request = createValidRequest();
            request.setDuration(1L);

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Отрицательная продолжительность должна вызывать ошибку")
        void negativeDuration_shouldFail() {
            FilmRequest request = createValidRequest();
            request.setDuration(-1L);

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .anyMatch(msg -> msg.contains("должно быть больше 0") ||
                            msg.contains("must be greater than 0"));
        }

        @Test
        @DisplayName("Большой положительный размер должен проходить")
        void largePositiveDuration_shouldPass() {
            FilmRequest request = createValidRequest();
            request.setDuration(1_000_000_000L);

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Валидация поля genre")
    class GenreValidationTests {

        @Test
        @DisplayName("null genre должен проходить (жанры опциональны)")
        void nullGenre_shouldPass() {
            FilmRequest request = createValidRequest();
            request.setGenres(null);

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Пустой список жанров должен проходить")
        void emptyGenre_shouldPass() {
            FilmRequest request = createValidRequest();
            request.setGenres(new ArrayList<>());

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("valid genre должен проходить")
        void validGenre_shouldPass() {
            FilmRequest request = createValidRequest();
            FilmRequest.GenreReference genre = new FilmRequest.GenreReference();
            genre.setId(1);
            request.setGenres(List.of(genre));

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Валидация поля mpaRating")
    class MpaRatingValidationTests {

        @Test
        @DisplayName("null mpaRating должен вызывать ошибку при создании")
        void nullMpaRating_shouldFailForCreate() {
            FilmRequest request = createValidRequest();
            request.setMpa(null);

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            // Проверяем, что есть ошибка для поля mpa
            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getPropertyPath)
                    .extracting(path -> path.toString())
                    .anyMatch(field -> field.equals("mpa"));
        }

        @Test
        @DisplayName("valid mpaRating должен проходить")
        void validMpaRating_shouldPass() {
            FilmRequest request = createValidRequest();
            FilmRequest.MpaReference mpa = new FilmRequest.MpaReference();
            mpa.setId(1);
            request.setMpa(mpa);

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Комплексная валидация FilmRequest")
    class FullValidationTests {

        @Test
        @DisplayName("Полностью валидный запрос должен проходить")
        void fullyValidFilm_shouldPass() {
            FilmRequest request = createValidRequest();

            Set<ConstraintViolation<FilmRequest>> violations =
                    validator.validate(request, ValidationGroups.Create.class);

            assertThat(violations).isEmpty();
        }
    }

    // ========== HELPER METHODS ==========

    private FilmRequest createValidRequest() {
        FilmRequest request = new FilmRequest();
        request.setName("Valid Movie");
        request.setDescription("Valid description");
        request.setReleaseDate(LocalDate.of(2024, 1, 1));
        request.setDuration(120L);

        FilmRequest.MpaReference mpa = new FilmRequest.MpaReference();
        mpa.setId(1);
        request.setMpa(mpa);

        return request;
    }
}