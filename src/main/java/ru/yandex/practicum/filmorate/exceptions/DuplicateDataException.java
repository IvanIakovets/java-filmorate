package ru.yandex.practicum.filmorate.exceptions;

public class DuplicateDataException extends RuntimeException {

    public DuplicateDataException(String message) {
        super(message);
    }
}
