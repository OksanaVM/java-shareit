package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingServiceIntegrationTest {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingRepository bookingRepository;
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
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        ItemDto itemDto = itemService.addItem(1L, itemDtoToCreate);

        BookingDto createdBooking = bookingService.addBooking(createdBooker.getId(), bookingToCreate);

        test(createdBooking, BookingStatus.WAITING, createdBooker, itemDto);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "false, REJECTED",
            "true, APPROVED"
    })
    void approveBooking_approve(boolean approve, BookingStatus status) {
        userService.addUser(owner);
        UserDto createdBooker = userService.addUser(booker);
        ItemDto itemDto = itemService.addItem(1L, itemDtoToCreate);
        bookingService.addBooking(createdBooker.getId(), bookingToCreate);
        BookingDto approvedBooking = bookingService.approve(1L, 1L, approve);

        test(approvedBooking, status, createdBooker, itemDto);
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
    void getAllBookingsByUser_ALL(String status, int start, int end, boolean approve) {
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
    void getAllBookingsByOwner_ALL(String status, int start, int end, boolean approve) {
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

}
