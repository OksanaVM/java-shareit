package ru.practicum.shareit.booking;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.util.HeaderConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
                                 @Valid @RequestBody @NotNull BookingDto bookingDto) {
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
    public List<BookingDto> getBooking(@RequestHeader(value = HeaderConstants.OWNER_ID) Long ownerId,
                                       @RequestParam(name = "state",
                                               required = false,
                                               defaultValue = "ALL") BookingStatus state,
                                       @RequestParam(required = false, name = "from") Integer from,
                                       @RequestParam(required = false, name = "size") Integer size) {
        return bookingService.getBooking(state, ownerId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookedItemList(@RequestHeader(value = HeaderConstants.OWNER_ID) Long ownerId,
                                                   @RequestParam(name = "state", required = false, defaultValue = "ALL") BookingStatus state,
                                                   @RequestParam(required = false, name = "from") Integer from,
                                                   @RequestParam(required = false, name = "size") Integer size) {
        return bookingService.ownerItemsBookingLists(state, ownerId, from, size);
    }
}
