package ru.practicum.shareit.booking.service;


import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;

import java.util.List;


public interface BookingService {
    BookingDto addBooking(Long bookerId, BookingDtoShort bookingDto);

    BookingDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingDto getBooking(Long bookerId, Long id);

    List<BookingDto> getBooking(String state, Long userId, int from, int size);

    List<BookingDto> ownerItemsBookingLists(String state, Long ownerId, int from, int size);
}

