package ru.practicum.shareit.booking.service;


import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;


public interface BookingService {
    BookingDto addBooking(Long bookerId, BookingDtoShort bookingDto);

    BookingDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingDto getBooking(Long bookerId, Long id);

    List<BookingDto> getBooking(BookingState state, Long userId, int from, int size);

    List<BookingDto> ownerItemsBookingLists(BookingState state, Long ownerId, int from, int size);
}

