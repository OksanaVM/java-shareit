package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class BookingServiceImplTest {
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private UserDto testUser;
    private UserDto secondTestUser;
    private ItemDto itemDtoFromDB;
    private BookingDtoShort bookingShortDto;
    private BookingDtoShort secondBookingShortDto;

    @BeforeEach
    public void setUp() {
        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();

        UserDto userDto = UserDto.builder()
                .email("test@test.com")
                .name("testName")
                .build();

        UserDto secondUserDto = UserDto.builder()
                .email("second@test.com")
                .name("secondName")
                .build();

        testUser = userService.addUser(userDto);
        secondTestUser = userService.addUser(secondUserDto);
        itemDtoFromDB = itemService.addItem(testUser.getId(), itemDto);

        bookingShortDto = BookingDtoShort.builder()
                .start(LocalDateTime.now().plusNanos(1))
                .end(LocalDateTime.now().plusNanos(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        secondBookingShortDto = BookingDtoShort.builder()
                .start(LocalDateTime.now().plusHours(3))
                .end(LocalDateTime.now().plusHours(4))
                .itemId(itemDtoFromDB.getId())
                .build();
    }

    @Test
    void createBookingTest() {
        BookingDto bookingDtoFromDB = bookingService.addBooking(secondTestUser.getId(), bookingShortDto);

        assertThat(bookingDtoFromDB.getId(), notNullValue());
        checkBookingsAreTheSame(bookingDtoFromDB, bookingShortDto, secondTestUser, itemDtoFromDB, BookingStatus.WAITING);
    }


    @Test
    void approveBookingTest() {
        BookingDto bookingDtoFromDB = bookingService.addBooking(secondTestUser.getId(), bookingShortDto);
        BookingDto approveBooking = bookingService.approve(testUser.getId(), bookingDtoFromDB.getId(), true);

        checkBookingsAreTheSame(approveBooking, bookingShortDto, secondTestUser, itemDtoFromDB, BookingStatus.APPROVED);
    }

    @Test
    void getBookingByIdTest() {
        BookingDto bookingDtoFromDB = bookingService.addBooking(secondTestUser.getId(), bookingShortDto);
        BookingDto approveBooking = bookingService.approve(testUser.getId(), bookingDtoFromDB.getId(), true);
        BookingDto bookingById = bookingService.getBooking(testUser.getId(), approveBooking.getId());

        checkBookingsAreTheSame(bookingById, bookingShortDto, secondTestUser, itemDtoFromDB, BookingStatus.APPROVED);

        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.getBooking(999L, approveBooking.getId()));
    }

    @Test
    void getAllBookingsTest() {
        List<BookingDtoShort> bookingDtos = List.of(bookingShortDto, secondBookingShortDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingShortDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), true);
        BookingDto secondBooking = bookingService.addBooking(secondTestUser.getId(), secondBookingShortDto);
        List<BookingDto> bookings = bookingService.getBooking(BookingState.ALL, secondTestUser.getId(), 0, 3);

        assertThat(bookings.size(), equalTo(bookingDtos.size()));
        for (BookingDtoShort dto : bookingDtos) {
            assertThat(bookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(dto.getStart())),
                    hasProperty("end", equalTo(dto.getEnd())))));
        }
        List<BookingDto> approvedBookings = bookingService.getBooking(BookingState.WAITING, secondTestUser.getId(), 0, 3);
        BookingDto waitingBooking = approvedBookings.get(0);

        assertThat(approvedBookings.size(), equalTo(1));
        assertThat(waitingBooking.getId(), equalTo(secondBooking.getId()));
        checkBookingsAreTheSame(waitingBooking, secondBookingShortDto, secondTestUser, itemDtoFromDB, BookingStatus.WAITING);
    }

    @Test
    void getAllOwnerBookingsTest() {
        List<BookingDtoShort> bookingDtos = List.of(bookingShortDto, secondBookingShortDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingShortDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), true);
        BookingDto secondBooking = bookingService.addBooking(secondTestUser.getId(), secondBookingShortDto);

        List<BookingDto> bookings = bookingService.ownerItemsBookingLists(BookingState.ALL, testUser.getId(), 0, 3);

        assertThat(bookings.size(), equalTo(bookingDtos.size()));
        for (BookingDtoShort dto : bookingDtos) {
            assertThat(bookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("start", equalTo(dto.getStart())),
                    hasProperty("end", equalTo(dto.getEnd())))));
        }

        List<BookingDto> approvedBookings = bookingService.ownerItemsBookingLists(BookingState.WAITING, testUser.getId(), 0, 3);
        BookingDto waitingBooking = approvedBookings.get(0);

        assertThat(approvedBookings.size(), equalTo(1));
        assertThat(waitingBooking.getId(), equalTo(secondBooking.getId()));
        checkBookingsAreTheSame(waitingBooking, secondBookingShortDto, secondTestUser, itemDtoFromDB, BookingStatus.WAITING);
    }

    @Test
    void approveBookingWrongOwnerTest() {
        BookingDto bookingDtoFromDB = bookingService.addBooking(secondTestUser.getId(), bookingShortDto);

        Assertions.assertThrows(NotFoundException.class,
                () -> bookingService.approve(secondTestUser.getId(), bookingDtoFromDB.getId(), true));
    }

    @Test
    void getAllBookingsRejectedStateTest() {
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingShortDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), false);

        List<BookingDto> rejectedBookings = bookingService.getBooking(BookingState.REJECTED, secondTestUser.getId(), 0, 3);
        BookingDto rejectedBooking = rejectedBookings.get(0);

        assertThat(rejectedBookings.size(), equalTo(1));
        checkBookingsAreTheSame(rejectedBooking, bookingShortDto, secondTestUser, itemDtoFromDB, BookingStatus.REJECTED);
    }

    @Test
    void getAllBookingsCurrentStateTest() {
        BookingDtoShort bookingDto = BookingDtoShort.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookingDtoShort> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> currentBookings = bookingService.getBooking(BookingState.CURRENT, secondTestUser.getId(), 0, 3);
        BookingDto currentBooking = currentBookings.get(0);

        assertThat(currentBookings.size(), equalTo(bookingDtos.size()));
        checkBookingsAreTheSame(currentBooking, bookingDto, secondTestUser, itemDtoFromDB, BookingStatus.APPROVED);
    }

    @Test
    void getAllBookingsFutureStateTest() {
        BookingDtoShort bookingDto = BookingDtoShort.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookingDtoShort> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingDto);

        List<BookingDto> futureBookings = bookingService.getBooking(BookingState.FUTURE, secondTestUser.getId(), 0, 3);
        BookingDto futureBooking = futureBookings.get(0);

        assertThat(futureBookings.size(), equalTo(bookingDtos.size()));
        assertThat(futureBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(futureBooking, bookingDto, secondTestUser, itemDtoFromDB, BookingStatus.WAITING);
    }

    @Test
    void getAllBookingsPastStateTest() {
        BookingDtoShort bookingDto = BookingDtoShort.builder()
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookingDtoShort> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> pastBookings = bookingService.getBooking(BookingState.PAST, secondTestUser.getId(), 0, 3);
        BookingDto pastBooking = pastBookings.get(0);

        assertThat(pastBookings.size(), equalTo(bookingDtos.size()));
        assertThat(pastBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(pastBooking, bookingDto, secondTestUser, itemDtoFromDB, BookingStatus.APPROVED);
    }

    @Test
    void getAllOwnerBookingsFutureStateTest() {
        BookingDtoShort bookingDto = BookingDtoShort.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookingDtoShort> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> futureBookings = bookingService.ownerItemsBookingLists(BookingState.FUTURE, testUser.getId(), 0, 3);
        BookingDto futureBooking = futureBookings.get(0);

        assertThat(futureBookings.size(), equalTo(bookingDtos.size()));
        assertThat(futureBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(futureBooking, bookingDto, secondTestUser, itemDtoFromDB, BookingStatus.APPROVED);
    }

    @Test
    void getAllOwnerBookingsPastStateTest() {
        BookingDtoShort bookingDto = BookingDtoShort.builder()
                .start(LocalDateTime.now().minusHours(2))
                .end(LocalDateTime.now().minusHours(1))
                .itemId(itemDtoFromDB.getId())
                .build();
        List<BookingDtoShort> bookingDtos = List.of(bookingDto);
        BookingDto firstBooking = bookingService.addBooking(secondTestUser.getId(), bookingDto);
        bookingService.approve(testUser.getId(), firstBooking.getId(), true);

        List<BookingDto> pastBookings = bookingService.ownerItemsBookingLists(BookingState.PAST, testUser.getId(), 0, 3);
        BookingDto pastBooking = pastBookings.get(0);

        assertThat(pastBookings.size(), equalTo(bookingDtos.size()));
        assertThat(pastBooking.getId(), equalTo(firstBooking.getId()));
        checkBookingsAreTheSame(pastBooking, bookingDto, secondTestUser, itemDtoFromDB, BookingStatus.APPROVED);
    }

    private void checkBookingsAreTheSame(
            BookingDto booking, BookingDtoShort secondBooking, UserDto user, ItemDto item, BookingStatus status) {
        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getStatus(), equalTo(status));
        assertThat(booking.getStart(), equalTo(secondBooking.getStart()));
        assertThat(booking.getEnd(), equalTo(secondBooking.getEnd()));
        assertThat(booking.getBooker().getId(), equalTo(user.getId()));
        assertThat(booking.getItem().getId(), equalTo(item.getId()));
        assertThat(booking.getItem().getName(), equalTo(item.getName()));
    }
}
