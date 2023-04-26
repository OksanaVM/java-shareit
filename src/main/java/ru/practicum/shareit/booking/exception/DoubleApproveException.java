package ru.practicum.shareit.booking.exception;

import ru.practicum.shareit.exceptions.BadRequestException;

public class DoubleApproveException extends BadRequestException {
    public DoubleApproveException(String message) {
        super(message);
    }
}