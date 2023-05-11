package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.dto.ItemBookingInfoDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;

    private final User user = new User(1L, "testUser", "test@email.com");
    private final UserDto userDto = new UserDto(1L, "testUser", "test@email.com");
    private final BookingDtoShort bookingDtoShort = BookingDtoShort.builder()
            .start(LocalDateTime.now())
            .end(LocalDateTime.now().plusHours(1L))
            .itemId(1L)
            .build();
    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("testItem")
            .description("testDescription")
            .available(true)
            .requestId(1L)
            .build();

    Item item = new Item(1L, "carpet", "description", true, null, null);

    private final Booking booking = Booking.builder()
            .booker(user)
            .id(1L)
            .status(BookingStatus.APPROVED)
            .item(item).build();


    @Test
    public void testGetBookingNotFound() {
        // Arrange
        Long bookerId = 1L;
        Long bookingId = 2L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        try {
            // Act
            bookingService.getBooking(bookerId, bookingId);
            fail("Expected NotFoundException to be thrown");
        } catch (NotFoundException ex) {
            // Assert
            assertEquals("Брони с таким id нет", ex.getMessage());
        }
    }

    @Test
    public void testGetBookingInvalidParameters() {
        // Arrange
        Long bookerId = 1L;
        Long bookingId = 2L;
        User booker = User.builder().id(4L).build();
        User owner = User.builder().id(3L).build();

        Item item = Item.builder().id(5L).owner(owner).build();
        Booking booking = Booking.builder()
                .id(bookingId)
                .booker(booker)
                .item(item)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.of(2023, 5, 12, 10, 0))
                .end(LocalDateTime.of(2023, 5, 14, 18, 0))
                .build();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        try {
            // Act
            bookingService.getBooking(bookerId, bookingId);
            fail("Expected NotFoundException to be thrown");
        } catch (NotFoundException ex) {
            // Assert
            assertEquals("Неверные параметры", ex.getMessage());
        }
    }


    @Test
    public void testId() {
        ItemBookingInfoDto itemBookingInfoDto = new ItemBookingInfoDto(1L, 2L, LocalDateTime.now(), LocalDateTime.now());
        assertEquals(1L, itemBookingInfoDto.getId());
    }

    @Test
    public void testBookerId() {
        ItemBookingInfoDto itemBookingInfoDto = new ItemBookingInfoDto(1L, 2L, LocalDateTime.now(), LocalDateTime.now());
        assertEquals(2L, itemBookingInfoDto.getBookerId());
    }

    @Test
    public void testStart() {
        LocalDateTime start = LocalDateTime.now();
        ItemBookingInfoDto itemBookingInfoDto = new ItemBookingInfoDto(1L, 2L, start, LocalDateTime.now());
        assertEquals(start, itemBookingInfoDto.getStart());
    }

    @Test
    public void testEnd() {
        LocalDateTime end = LocalDateTime.now();
        ItemBookingInfoDto itemBookingInfoDto = new ItemBookingInfoDto(1L, 2L, LocalDateTime.now(), end);
        assertEquals(end, itemBookingInfoDto.getEnd());
    }

    @Test
    public void testNullId() {
        ItemBookingInfoDto itemBookingInfoDto = new ItemBookingInfoDto(null, 2L, LocalDateTime.now(), LocalDateTime.now());
        assertNull(itemBookingInfoDto.getId());
    }

    @Test
    public void testNullBookerId() {
        ItemBookingInfoDto itemBookingInfoDto = new ItemBookingInfoDto(1L, null, LocalDateTime.now(), LocalDateTime.now());
        assertNull(itemBookingInfoDto.getBookerId());
    }

    @Test
    public void testNullStart() {
        ItemBookingInfoDto itemBookingInfoDto = new ItemBookingInfoDto(1L, 2L, null, LocalDateTime.now());
        assertNull(itemBookingInfoDto.getStart());
    }

    @Test
    public void testNullEnd() {
        ItemBookingInfoDto itemBookingInfoDto = new ItemBookingInfoDto(1L, 2L, LocalDateTime.now(), null);
        assertNull(itemBookingInfoDto.getEnd());
    }

    @Test
    public void testGetId() {
        Comment comment = new Comment(1L, "text", new Item(), new User(), LocalDateTime.now());
        assertEquals(1L, comment.getId());
    }

    @Test
    public void testSetId() {
        Comment comment = new Comment();
        comment.setId(2L);
        assertEquals(2L, comment.getId());
    }

    @Test
    public void testGetText() {
        Comment comment = new Comment(1L, "text", new Item(), new User(), LocalDateTime.now());
        assertEquals("text", comment.getText());
    }

    @Test
    public void testSetText() {
        Comment comment = new Comment();
        comment.setText("newText");
        assertEquals("newText", comment.getText());
    }

    @Test
    public void testGetItem() {
        Item item = new Item();
        Comment comment = new Comment(1L, "text", item, new User(), LocalDateTime.now());
        assertEquals(item, comment.getItem());
    }

    @Test
    public void testSetItem() {
        Item item = new Item();
        Comment comment = new Comment();
        comment.setItem(item);
        assertEquals(item, comment.getItem());
    }

    @Test
    public void testGetAuthor() {
        User user = new User();
        Comment comment = new Comment(1L, "text", new Item(), user, LocalDateTime.now());
        assertEquals(user, comment.getAuthor());
    }

    @Test
    public void testSetAuthor() {
        User user = new User();
        Comment comment = new Comment();
        comment.setAuthor(user);
        assertEquals(user, comment.getAuthor());
    }

    @Test
    public void testGetCreated() {
        LocalDateTime dateTime = LocalDateTime.now();
        Comment comment = new Comment(1L, "text", new Item(), new User(), dateTime);
        assertEquals(dateTime, comment.getCreated());
    }

    @Test
    public void testSetCreated() {
        LocalDateTime dateTime = LocalDateTime.now();
        Comment comment = new Comment();
        comment.setCreated(dateTime);
        assertEquals(dateTime, comment.getCreated());
    }


}
