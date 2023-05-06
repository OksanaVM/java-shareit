package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import ru.practicum.shareit.exceptions.RequestFailedException;

@AllArgsConstructor
public enum BookingState {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static BookingState getStateFromText(String text) {
        for (BookingState state : BookingState.values()) {
            if (state.toString().equals(text)) {
                return state;
            }
        }
        throw new RequestFailedException("Unknown state: " + text);
    }
}
