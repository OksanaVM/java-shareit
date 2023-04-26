package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.List;

@Service
public interface BookingService {
    BookingDto addBooking(Long bookerId, BookingDto bookingDto);

    BookingDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingDto getBooking(Long bookerId, Long id);

    List<BookingDto> getBooking(BookingStatus state, Long bookerId, Integer from, Integer size);

    List<BookingDto> ownerItemsBookingLists(BookingStatus state, Long ownerId, Integer from, Integer size);
}

