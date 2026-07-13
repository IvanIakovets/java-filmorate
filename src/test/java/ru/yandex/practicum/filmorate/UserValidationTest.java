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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование валидации модели User")
class UserValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Валидация поля email")
    class EmailValidationTests {

        @ParameterizedTest
        @MethodSource("validEmailProvider")
        @DisplayName("Валидные email должны проходить проверку")
        void validEmail_shouldPass(String email) {
            User user = createValidUser();
            user.setEmail(email);

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("invalidEmailProvider")
        @DisplayName("Невалидные email должны вызывать ошибку")
        void invalidEmail_shouldFail(String email) {
            User user = createValidUser();
            user.setEmail(email);

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getPropertyPath)
                    .extracting(path -> path.toString())
                    .contains("email");
        }

        @Test
        @DisplayName("null email должен вызывать ошибку @NotBlank")
        void nullEmail_shouldFail() {
            User user = createValidUser();
            user.setEmail(null);

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Email не может быть пустым");
        }

        static Stream<Arguments> validEmailProvider() {
            return Stream.of(
                    Arguments.of("user@example.com"),
                    Arguments.of("user.name@example.com"),
                    Arguments.of("user+filter@example.com"),
                    Arguments.of("user@sub.domain.com"),
                    Arguments.of("user@example.co.uk"),
                    Arguments.of("user@example.io"),
                    Arguments.of("123@example.com")
            );
        }

        static Stream<Arguments> invalidEmailProvider() {
            return Stream.of(
                    Arguments.of(""),
                    Arguments.of("   "),
                    Arguments.of("plainaddress"),
                    Arguments.of("@missingusername.com"),
                    Arguments.of("username@.com"),
                    Arguments.of("username@domain..com"),
                    Arguments.of("user2domain.com"),
                    Arguments.of("user@domain@toolongtld"),
                    Arguments.of("user name@example.com"),
                    Arguments.of("user-example")
            );
        }
    }

    @Nested
    @DisplayName("Валидация поля login")
    class LoginValidationTests {

        @ParameterizedTest
        @MethodSource("validLoginProvider")
        @DisplayName("Валидные логины должны проходить проверку")
        void validLogin_shouldPass(String login) {
            User user = createValidUser();
            user.setLogin(login);

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("invalidLoginProvider")
        @DisplayName("Невалидные логины должны вызывать ошибку")
        void invalidLogin_shouldFail(String login) {
            User user = createValidUser();
            user.setLogin(login);

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getPropertyPath)
                    .extracting(path -> path.toString())
                    .contains("login");
        }

        @Test
        @DisplayName("null login должен вызывать ошибку @NotBlank")
        void nullLogin_shouldFail() {
            User user = createValidUser();
            user.setLogin(null);

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Логин не может быть пустым");
        }

        static Stream<Arguments> validLoginProvider() {
            return Stream.of(
                    Arguments.of("user123"),
                    Arguments.of("user_name"),
                    Arguments.of("user-name"),
                    Arguments.of("user%^8"),
                    Arguments.of("a"),
                    Arguments.of("User123"),
                    Arguments.of("user_login_123")
            );
        }

        static Stream<Arguments> invalidLoginProvider() {
            return Stream.of(
                    Arguments.of(""),
                    Arguments.of("   "),
                    Arguments.of("32 24"),
                    Arguments.of("?^& &*^"),
                    Arguments.of("%# 323 rd"),
                    Arguments.of(" user"),
                    Arguments.of("user ")
            );
        }

        @Test
        @DisplayName("Сообщение об ошибке для login с пробелами должно быть корректным")
        void loginWithSpaces_shouldHaveCorrectMessage() {
            User user = createValidUser();
            user.setLogin("user name");

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Логин не должен содержать пробелы");
        }
    }

    @Nested
    @DisplayName("Валидация поля name")
    class NameValidationTests {

        @Test
        @DisplayName("null name должен проходить валидацию (поле опционально)")
        void nullName_shouldPass() {
            User user = createValidUser();
            user.setName(null);

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Пустой name должен проходить валидацию (поле опционально)")
        void emptyName_shouldPass() {
            User user = createValidUser();
            user.setName("");

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Пробельный name должен проходить валидацию")
        void blankName_shouldPass() {
            User user = createValidUser();
            user.setName("   ");

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Name длиной ровно 100 символов должен проходить")
        void nameLengthExactly100_shouldPass() {
            User user = createValidUser();
            user.setName("a".repeat(100));

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Name длиной 101 символ должен вызывать ошибку")
        void nameLength101_shouldFail() {
            User user = createValidUser();
            user.setName("a".repeat(101));

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getPropertyPath)
                    .extracting(path -> path.toString())
                    .contains("name");
        }

        @Test
        @DisplayName("Сообщение об ошибке для слишком длинного name должно быть корректным")
        void tooLongName_shouldHaveCorrectMessage() {
            User user = createValidUser();
            user.setName("a".repeat(101));

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Имя не должно превышать 100 символов");
        }
    }

    @Nested
    @DisplayName("Валидация поля birthDate")
    class BirthDateValidationTests {

        @Test
        @DisplayName("null birthDate должен вызывать ошибку @NotNull")
        void nullBirthDate_shouldFail() {
            User user = createValidUser();
            user.setBirthDate(null);

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Дата рождения не может быть пустой");
        }

        @Test
        @DisplayName("Дата рождения сегодня не должна проходить @Past")
        void todayBirthDate_shouldPass() {
            User user = createValidUser();
            user.setBirthDate(LocalDate.now());

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Дата рождения должна быть в прошлом");
        }

        @Test
        @DisplayName("Дата рождения вчера должна проходить")
        void yesterdayBirthDate_shouldPass() {
            User user = createValidUser();
            user.setBirthDate(LocalDate.now().minusDays(1));

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Дата рождения завтра должна вызывать ошибку")
        void tomorrowBirthDate_shouldFail() {
            User user = createValidUser();
            user.setBirthDate(LocalDate.now().plusDays(1));

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations)
                    .isNotEmpty()
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Дата рождения должна быть в прошлом");
        }

        @Test
        @DisplayName("Очень старая дата рождения должна проходить")
        void veryOldBirthDate_shouldPass() {
            User user = createValidUser();
            user.setBirthDate(LocalDate.of(1900, 1, 1));

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Комплексная валидация User")
    class FullValidationTests {

        @Test
        @DisplayName("Полностью валидный пользователь должен проходить")
        void fullyValidUser_shouldPass() {
            User user = createValidUser();

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Пользователь со всеми невалидными полями должен возвращать все ошибки")
        void allInvalidFields_shouldReturnAllErrors() {
            User user = User.builder()
                    .email("invalid")
                    .login("invalid login")
                    .name("a".repeat(101))
                    .birthDate(LocalDate.now().plusDays(1))
                    .build();

            Set<ConstraintViolation<User>> violations = validator.validate(user);

            assertThat(violations).hasSize(4);

            assertThat(violations)
                    .extracting(ConstraintViolation::getPropertyPath)
                    .extracting(path -> path.toString())
                    .containsExactlyInAnyOrder("email", "login", "name", "birthDate");
        }
    }

    private User createValidUser() {
        return User.builder()
                .email("user@example.com")
                .login("validLogin")
                .name("Valid Name")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
    }
}
