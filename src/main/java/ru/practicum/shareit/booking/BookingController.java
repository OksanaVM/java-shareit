package ru.practicum.shareit.booking;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.util.HeaderConstants;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto addBooking(@RequestHeader(value = HeaderConstants.OWNER_ID) Long bookerId,
                                 @Validated @RequestBody BookingDtoShort bookingDto) {
        return bookingService.addBooking(bookerId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBooking(@RequestHeader(value = HeaderConstants.OWNER_ID) Long ownerId,
                                    @PathVariable Long bookingId,
                                    @RequestParam(name = "approved") boolean approved) {
        return bookingService.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader(value = HeaderConstants.OWNER_ID) Long bookerId,
                                 @PathVariable Long bookingId) {
        return bookingService.getBooking(bookerId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBooking(@RequestParam(defaultValue = "ALL") String state,
                                       @RequestHeader(value = HeaderConstants.OWNER_ID) Long userId,
                                       @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                       @Positive @RequestParam(defaultValue = "10") Integer size) {
        return bookingService.getBooking(state, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookedItemList(@RequestParam(defaultValue = "ALL") String state,
                                                   @RequestHeader(value = HeaderConstants.OWNER_ID) Long userId,
                                                   @RequestParam(value = "size", defaultValue = "10") @Positive int size,
                                                   @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from) {
        return bookingService.ownerItemsBookingLists(state, userId, from, size);
    }
}
