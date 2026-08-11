package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exeptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exeptions.DuplicateDataException;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Ошибка валидации: {}", ex.getMessage());

        Map<String, String> validationErrors = ex.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap(
                        error -> ((FieldError) error).getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing
                ));


        return ErrorResponse.builder()
                .message("Ошибка валидации данных")
                .errorCode("VALIDATION_ERROR")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();
    }

    @ExceptionHandler(ConditionsNotMetException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(Exception ex) {
        log.error("Ошибка запроса: {}", ex.getMessage());
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode("BAD_REQUEST")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(DuplicateDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicate(DuplicateDataException ex) {
        log.error("Конфликт данных: {}", ex.getMessage());
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode("CONFLICT")
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException ex) {
        log.error("Объект не найден: {}", ex.getMessage());
        return ErrorResponse.builder()
                .message(ex.getMessage())
                .errorCode("NOT_FOUND")
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParams(MissingServletRequestParameterException ex) {
        log.error("Отсутствует обязательный параметр: {}", ex.getMessage());
        return ErrorResponse.builder()
                .message("Отсутствует обязательный параметр: " + ex.getParameterName())
                .errorCode("MISSING_PARAMETER")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotReadable(HttpMessageNotReadableException ex) {
        log.error("Некорректный формат запроса: {}", ex.getMessage());
        return ErrorResponse.builder()
                .message("Некорректный формат запроса. Проверьте структуру JSON.")
                .errorCode("BAD_REQUEST")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneralException(Exception ex) {
        log.error("Непредвиденная ошибка: ", ex);
        return ErrorResponse.builder()
                .message("Внутренняя ошибка сервера. Пожалуйста, попробуйте позже.") // ← Скрываем детали
                .errorCode("INTERNAL_SERVER_ERROR")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
