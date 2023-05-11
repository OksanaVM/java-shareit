package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.AuthorDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemsDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentDto commentDto;
    @Mock
    private BookingService bookingService;


    @Test
    void addItem_whenValidParametersProvided_thenItemAdded() {
        Long ownerId = 1L;
        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", true, null);
        Mockito.when(userRepository.findById(ownerId)).thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.save(Mockito.any(Item.class))).thenReturn(new Item());

        ItemDto result = itemService.addItem(ownerId, itemDto);

        Assertions.assertNotNull(result);
    }

    @Test
    public void shouldSuccessUpdateItem() {

        User owner = new User(1L, "eee@email.ru", "Eva");
        Item item = new Item(1L, "crrr", "description", true, null, owner);


        Mockito.when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(owner));

        Item oldItem = new Item(2L, "плед", "warm", true, null, owner);
        Mockito.when(itemRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(any())).thenReturn(item);

        ItemDto newItem = new ItemDto(2L, "crrr", "description", true, null);

        Assertions.assertNotEquals(oldItem.getDescription(), newItem.getDescription());

        ItemDto updatedItem = itemService.update(owner.getId(), oldItem.getId(), newItem);

        assertEquals(updatedItem.getName(), newItem.getName());
        assertEquals(updatedItem.getDescription(), newItem.getDescription());
    }

    @Test
    public void testGetExistingItem() {
        Long itemId = 1L;
        Long userId = 2L;
        User owner = new User(1L, "John", "Doe");
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(new Item(itemId, "Test Item", "This is a test item", true, null, owner)));
        Mockito.when(commentRepository.findByItemIn(Mockito.anyList(), Mockito.any(Sort.class))).thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findByItemInAndStatus(Mockito.anyList(), eq(BookingStatus.APPROVED), Mockito.any(Sort.class))).thenReturn(Collections.emptyList());

        ItemsDto result = itemService.getItem(itemId, userId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals("This is a test item", result.getDescription());
        assertTrue(result.getAvailable());
        assertNull(result.getRequestId());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertTrue(result.getComments().isEmpty());
    }


    @Test
    public void getItem_invalidInput_throwsNotFoundException() {
        Long itemId = 1L;
        Long userId = 2L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItem(itemId, userId));
    }

    @Test
    public void getItems_invalidOwnerId_throwsException() {
        Long ownerId = -1L;
        int from = 0;
        int size = 10;

        assertThrows(IllegalArgumentException.class, () -> itemService.getItems(ownerId, from, size));
    }

    @Test
    public void getItems_invalidFromValue_throwsException() {
        Long ownerId = 1L;
        int from = -1;
        int size = 10;

        assertThrows(IllegalArgumentException.class, () -> itemService.getItems(ownerId, from, size));
    }

    @Test
    public void testGetItems_Negative() {
        Long ownerId = null;
        int from = 0;
        int size = 10;

        try {
            itemService.getItems(ownerId, from, size);
            fail("Expected NotFoundException");
        } catch (NotFoundException e) {

            assertEquals("Пользователь не найден", e.getMessage());
        }
    }

    @Test
    public void testToItemDtoList_withValidInput_shouldReturnCorrectOutput() {
        List<Item> itemList = new ArrayList<>();
        itemList.add(new Item(1L, "test1", "description1", true, null, null));
        itemList.add(new Item(2L, "test2", "description2", false, null, null));
        List<ItemDto> expectedResult = new ArrayList<>();
        expectedResult.add(new ItemDto(1L, "test1", "description1", true, null));
        expectedResult.add(new ItemDto(2L, "test2", "description2", false, null));

        List<ItemDto> actualResult = ItemMapper.toItemDtoList(itemList);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testToItemDtoList_withEmptyList_shouldReturnEmptyList() {
        List<Item> itemList = Collections.emptyList();
        List<ItemDto> expectedResult = Collections.emptyList();

        List<ItemDto> actualResult = ItemMapper.toItemDtoList(itemList);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testFindAllItemsByLike_withBlankText_shouldReturnEmptyList() {
        String text = "";
        Pageable page = PageRequest.of(0, 10);
        List<Item> expectedResult = Collections.emptyList();

        List<Item> actualResult = itemRepository.findAllItemsByLike(text, page);

        assertEquals(expectedResult, actualResult);
    }


    @Test
    public void testGetItems() {
        String searchText = "test";
        int from = 0;
        int size = 10;
        Pageable page = PageRequest.of(from / size, size);
        List<Item> itemList = Arrays.asList(
                new Item(1L, "Test Item 1", "This is a test item 1", true, null, null),
                new Item(2L, "Test Item 2", "This is a test item 2", true, null, null),
                new Item(3L, "Another Item", "This is another item", true, null, null)
        );
        Mockito.when(itemRepository.findAllItemsByLike(eq(searchText), eq(page))).thenReturn(itemList);

        List<ItemDto> result = itemService.getItems(searchText, from, size);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).getId().longValue());
        assertEquals("Test Item 1", result.get(0).getName());
        assertEquals("This is a test item 1", result.get(0).getDescription());
        assertTrue(result.get(0).getAvailable());
        assertNull(result.get(0).getRequestId());
        assertEquals(2L, result.get(1).getId().longValue());
        assertEquals("Test Item 2", result.get(1).getName());
        assertEquals("This is a test item 2", result.get(1).getDescription());
        assertTrue(result.get(1).getAvailable());
        assertNull(result.get(1).getRequestId());
    }

    @Test
    public void testAddComment_Negative() {
        Long authorId = 1L;
        Long itemId = 3L;
        CommentDto commentDto = new CommentDto(1L, "This is a test comment", null, "z", LocalDateTime.now());
        Exception exception = assertThrows(NotFoundException.class, () -> itemService.addComment(authorId, itemId, commentDto));
        assertEquals("Автор не найден", exception.getMessage());
    }

    @Test
    public void testGettersSetters() {
        commentDto = CommentDto.builder()
                .id(1L)
                .text("This is a comment")
                .author(AuthorDto.builder().id(2L).authorName("John").build())
                .authorName("John")
                .created(LocalDateTime.now())
                .build();

        Long newId = 0L;
        String newText = "Updated comment";
        AuthorDto newAuthor = AuthorDto.builder().id(4L).authorName("Jane").build();
        String newAuthorName = "Jane";
        LocalDateTime newCreated = LocalDateTime.now().plusDays(1);

        commentDto.setId(newId);
        commentDto.setText(newText);
        commentDto.setAuthor(newAuthor);
        commentDto.setAuthorName(newAuthorName);
        commentDto.setCreated(newCreated);

        assertEquals(newId, commentDto.getId());
        assertEquals(newText, commentDto.getText());
        assertEquals(newAuthor, commentDto.getAuthor());
        assertEquals(newAuthorName, commentDto.getAuthorName());
        assertEquals(newCreated, commentDto.getCreated());
    }

    @Test
    public void testToString() {
        commentDto = CommentDto.builder()
                .id(1L)
                .text("This is a comment")
                .author(AuthorDto.builder().id(2L).authorName("John").build())
                .authorName("John")
                .created(LocalDateTime.now())
                .build();

        String expectedString = "CommentDto(id=1, text=This is a comment, author=AuthorDto(id=2, authorName=John, email=null), authorName=John, created=" + commentDto.getCreated().toString() + ")";

        String resultString = commentDto.toString();

        assertEquals(expectedString, resultString);
    }

    @Test
    void shouldCreateComment() {
        Item item1 = new Item(1L, "item1", "description Item1", true, 1L, null);
        User user1 = new User(1L, "userName", "user@mail.ru");
        Booking bookingLast = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), item1, user1, BookingStatus.APPROVED);
        Comment comment1 = new Comment(1L, "text comment1", item1, user1, LocalDateTime.now());
        CommentDto commentDto1 = CommentMapper.toCommentDto(comment1);
        CommentDto commentDtoOutput = CommentMapper.toCommentDto(comment1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(bookingRepository.findBookingsByItem(any(), eq(BookingStatus.APPROVED), anyLong(), any(LocalDateTime.class))).thenReturn(List.of(bookingLast));
        when(commentRepository.save(any())).thenReturn(comment1);

        CommentDto commentDtoOutputAfter = itemService.addComment(1L, 1L, commentDto1);
        assertEquals(commentDtoOutput.getId(), commentDtoOutputAfter.getId());
        assertEquals(commentDtoOutput.getText(), commentDtoOutputAfter.getText());
        assertEquals(commentDtoOutput.getAuthorName(), commentDtoOutputAfter.getAuthorName());
        assertEquals(commentDtoOutput.getCreated(), commentDtoOutputAfter.getCreated());
    }


}



