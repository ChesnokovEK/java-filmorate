package ru.yandex.practicum.filmorate.exception;

public class DoesNotExistsException extends RuntimeException {
    public DoesNotExistsException(String m) {
        super(m);
    }
}
