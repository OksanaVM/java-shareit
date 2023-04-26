package ru.practicum.shareit.exceptions;

public class MissingIdException extends RuntimeException {

    public MissingIdException(final String message) {
        super(message);
    }
}