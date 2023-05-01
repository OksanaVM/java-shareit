package ru.practicum.shareit.booking.service;


import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;


public interface BookingService {
    BookingDto addBooking(Long bookerId, BookingDto bookingDto);

    BookingDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingDto getBooking(Long bookerId, Long id);

    List<BookingDto> getBooking(String state, Long bookerId, Integer from, Integer size);

    List<BookingDto> ownerItemsBookingLists(String state, Long ownerId, Integer from, Integer size);
}

