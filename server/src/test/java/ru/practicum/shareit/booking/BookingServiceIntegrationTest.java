package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.dto.ItemBookingInfoDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.RequestFailedException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.booking.mapper.BookingMapper.toItemBookingInfoDto;


@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingServiceIntegrationTest {

    private final BookingService bookingService;
    private final BookingServiceImpl bookingServicei;
    private final UserService userService;
    private final ItemService itemService;

    private final UserDto owner = new UserDto(null, "testUser", "test@email.com");
    private final UserDto booker = new UserDto(null, "testUser2", "test2@email.com");
    private final ItemDto itemDtoToCreate = ItemDto.builder().name("testItem").description("testDescription").available(true).build();
    private final BookingDtoShort bookingToCreate = BookingDtoShort.builder().itemId(1L).start(LocalDateTime.now().plusHours(1))
            .end(LocalDateTime.now().plusHours(2)).build();

    void test(BookingDto booking, BookingStatus status, UserDto createdBooker, ItemDto itemDto) {
        assertThat(booking.getId(), equalTo(1L));
        assertThat(booking.getStart(), equalTo(bookingToCreate.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingToCreate.getEnd()));
        assertThat(booking.getBooker(), equalTo(createdBooker));
        assertThat(booking.getItem().getId(), equalTo(itemDto.getId()));
        assertThat(booking.getStatus(), equalTo(status));
    }

    @Test
    void createBooking() {
        UserDto createdOwner = userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        ItemDto itemDto = itemService.addItem(createdOwner.getId(), itemDtoToCreate);

        BookingDto createdBooking = bookingService.addBooking(createdBooker.getId(), bookingToCreate);

        test(createdBooking, BookingStatus.WAITING, createdBooker, itemDto);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "ALL, 1, 2, true",
            "CURRENT, -1, 2, true",
            "PAST, -2, -1, true",
            "FUTURE, 1, 2, true",
            "WAITING, 1, 2, true",
            "REJECTED, 1, 2, false"
    })
    void getAllBookingsByUser_ALL(BookingState status, int start, int end, boolean approve) {
        BookingDtoShort booking = BookingDtoShort.builder().itemId(1L)
                .start(LocalDateTime.now().plusHours(start))
                .end(LocalDateTime.now().plusHours(end)).build();
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(1L, itemDtoToCreate);
        BookingDto bookingDto = bookingService.addBooking(createdBooker.getId(), booking);
        if (!approve) {
            bookingService.approve(bookingDto.getId(), 1L, approve);
        }
        List<BookingDto> userBookingsList = bookingService.getBooking(status, 2L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "ALL, 1, 2, true",
            "CURRENT, -1, 2, true",
            "PAST, -2, -1, true",
            "FUTURE, 1, 2, true",
            "WAITING, 1, 2, true",
            "REJECTED, 1, 2, false"
    })
    void getAllBookingsByOwner_ALL(BookingState status, int start, int end, boolean approve) {
        BookingDtoShort booking = BookingDtoShort.builder().itemId(1L)
                .start(LocalDateTime.now().plusHours(start))
                .end(LocalDateTime.now().plusHours(end)).build();
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        itemService.addItem(1L, itemDtoToCreate);
        BookingDto bookingDto = bookingService.addBooking(createdBooker.getId(), booking);
        if (!approve) {
            bookingService.approve(bookingDto.getId(), 1L, approve);
        }
        List<BookingDto> userBookingsList = bookingService.ownerItemsBookingLists(status, 1L, 0, 10);
        assertThat(userBookingsList, hasSize(1));
        assertThat(userBookingsList.get(0).getId(), equalTo(bookingDto.getId()));
    }

    @Test
    public void testGetStateFromText() {
        assertEquals(BookingState.ALL, BookingState.getStateFromText("ALL"));
        assertEquals(BookingState.CURRENT, BookingState.getStateFromText("CURRENT"));
        assertEquals(BookingState.PAST, BookingState.getStateFromText("PAST"));
        assertEquals(BookingState.FUTURE, BookingState.getStateFromText("FUTURE"));
        assertEquals(BookingState.WAITING, BookingState.getStateFromText("WAITING"));
        assertEquals(BookingState.REJECTED, BookingState.getStateFromText("REJECTED"));
        try {
            BookingState.getStateFromText("INVALID");
            fail("Expected an RequestFailedException to be thrown");
        } catch (RequestFailedException e) {
            assertEquals("Unknown state: INVALID", e.getMessage());
        }
    }

    @Test
    public void testGetStateFromText_InvalidInput() {
        String text = "INVALID";
        RequestFailedException exception = assertThrows(RequestFailedException.class, () -> {
            BookingState.getStateFromText(text);
        });
        assertEquals("Unknown state: INVALID", exception.getMessage());
    }

    @Test
    public void testToItemBookingInfoDto_positive() {

        Booking booking = Booking.builder()
                .id(1L)
                .booker(User.builder().id(2L).build())
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .build();

        ItemBookingInfoDto itemBookingInfoDto = toItemBookingInfoDto(booking);

        assertEquals(1L, itemBookingInfoDto.getId());
        assertEquals(2L, itemBookingInfoDto.getBookerId());
        assertEquals(booking.getStart(), itemBookingInfoDto.getStart());
        assertEquals(booking.getEnd(), itemBookingInfoDto.getEnd());
    }

    @Test
    public void testToItemBookingInfoDto_negative() {

        Booking booking = Booking.builder()
                .id(1L)
                .booker(User.builder().id(2L).build())
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .build();

        ItemBookingInfoDto itemBookingInfoDto = toItemBookingInfoDto(booking);

        assertNotEquals(2L, itemBookingInfoDto.getId());
        assertNotEquals(1L, itemBookingInfoDto.getBookerId());
        assertNotEquals(booking.getStart(), itemBookingInfoDto.getEnd());
        assertNotEquals(booking.getEnd(), itemBookingInfoDto.getStart());
    }

    @Test
    public void approve_withInvalidOwnerId_shouldThrowNotFoundException() {
        Long ownerId = 3L;
        Long bookingId = 2L;
        boolean approved = true;

        Exception exception = assertThrows(NotFoundException.class, () -> bookingService.approve(ownerId, bookingId, approved));


        assertEquals("Брони с такой ID нет", exception.getMessage());
    }

    @Test
    public void approve_withInvalidBookingId_shouldThrowNotFoundException() {
        Long ownerId = 1L;
        Long bookingId = 4L;
        boolean approved = true;

        Exception exception = assertThrows(NotFoundException.class, () -> bookingService.approve(ownerId, bookingId, approved));

        assertEquals("Брони с такой ID нет", exception.getMessage());
    }


}
