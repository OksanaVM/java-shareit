package ru.practicum.shareit.exceptions;

public class RequestFailedException extends RuntimeException {

    public RequestFailedException(final String message) {
        super(message);
    }
}
