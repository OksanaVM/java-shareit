package ru.practicum.shareit.booking.exception;
import ru.practicum.shareit.exceptions.BadRequestException;

public class BookingUnavailableException extends BadRequestException {
    public BookingUnavailableException(String message) {
        super(message);
    }
}
