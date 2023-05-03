package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BookingState {
    ALL("ALL"),
    CURRENT("CURRENT"),
    PAST("PAST"),
    FUTURE("FUTURE"),
    WAITING("WAITING"),
    REJECTED("REJECTED");

    public final String label;
}
