package ru.practicum.shareit.booking.exception;

import ru.practicum.shareit.exceptions.BadRequestException;

public class TimeDataException extends BadRequestException {
    public TimeDataException(String message) {
        super(message);
    }
}