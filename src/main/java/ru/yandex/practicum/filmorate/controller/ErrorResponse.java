package ru.yandex.practicum.filmorate.controller;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ErrorResponse {
    private int status; // HTTP-статус ошибки
    private String message;// описание ошибки
    private LocalDateTime timestamp; //временной штамп
    private String errorCode; // код ошибки для програмной обработки

    private Map<String, String> validationErrors;
}