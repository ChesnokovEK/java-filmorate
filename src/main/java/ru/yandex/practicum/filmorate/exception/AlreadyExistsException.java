package ru.yandex.practicum.filmorate.exception;

public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String m) {
        super(m);
    }
}