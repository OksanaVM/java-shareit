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
import ru.practicum.shareit.exceptions.IncorrectEntityParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.AuthorDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.OutputItemDto;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    @Test
    void addItem_whenValidParametersProvided_thenItemAdded() {
        Long ownerId = 1L;
        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", true, null);
        Mockito.when(userRepository.existsById(ownerId)).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
        Mockito.when(itemRepository.save(Mockito.any(Item.class))).thenReturn(new Item());

        ItemDto result = itemService.addItem(ownerId, itemDto);

        Assertions.assertNotNull(result);
    }

    @Test
    public void update_validOwnerIdAndItemId_returnsUpdatedItemDto() {
        Long ownerId = 1L;
        Long itemId = 2L;
        ItemDto itemDto = new ItemDto(1L, "test1", "description1", true, null);
        itemDto.setName("test");
        itemDto.setDescription("test description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(3L);
        User user = new User();
        user.setId(ownerId);
        Item oldItem = new Item();
        oldItem.setId(itemId);
        oldItem.setOwner(user);
        oldItem.setName("old name");
        oldItem.setDescription("old description");
        oldItem.setIsAvailable(false);
        oldItem.setRequestId(4L);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(oldItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

        ItemDto result = itemService.update(ownerId, itemId, itemDto);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("test", result.getName());
        assertEquals("test description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(3L, result.getRequestId());
    }

    @Test
    public void testGetExistingItem() {
        Long itemId = 1L;
        Long userId = 2L;
        User owner = new User(1L, "John", "Doe");
        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(new Item(itemId, "Test Item", "This is a test item", true, null, owner)));
        Mockito.when(commentRepository.findByItemIn(Mockito.anyList(), Mockito.any(Sort.class))).thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findByItemInAndStatus(Mockito.anyList(), eq(BookingStatus.APPROVED), Mockito.any(Sort.class))).thenReturn(Collections.emptyList());

        OutputItemDto result = itemService.getItem(itemId, userId);

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
    public void update_invalidOwnerId_throwsNotFoundException() {
        Long ownerId = 1L;
        Long itemId = 2L;
        ItemDto itemDto = new ItemDto(1L, "test1", "description1", true, null);
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.update(ownerId, itemId, itemDto));
    }
//
//    @Test
//    public void shouldFailAddItemWithIncorrectParam() {
//        User owner = new User(1L, "eee@email.ru", "Eva");
//
//        ItemDto newItem = new ItemDto(null, null, null, null, null);
//        IncorrectEntityParameterException exception = assertThrows(IncorrectEntityParameterException.class, () -> itemService.addItem(owner.getId(), newItem));
//
//        ItemDto newItemWithoutName = new ItemDto(null, null, null, true, null);
//        assertThrows(IncorrectEntityParameterException.class, () -> itemService.addItem(owner.getId(), newItemWithoutName));
//        Assertions.assertNotNull(exception);
//
//        ItemDto newItemWithoutDescription = new ItemDto(null, "name", null, true, null);
//        assertThrows(IncorrectEntityParameterException.class, () -> itemService.addItem(owner.getId(), newItemWithoutDescription));
//
//    }

//    @Test
//    public void testAddItemNullOwnerId() {
//        ItemDto itemDto = new ItemDto(null, "Laptop", "A high-performance laptop", true, null);
//
//        assertThrows(IncorrectEntityParameterException.class, () -> {
//            itemService.addItem(null, itemDto);
//        });
//    }

    @Test
    public void shouldMapToCommentDtoList() {
        User owner = new User(1L, "eee@email.ru", "Eva");
        Item item = new Item(1L, "carpet", "description", true, null, owner);

        User author = new User(3L, "sabrina@email.ru", "Sabrina");
        Comment comment1 = new Comment(1L, "text1", item, author, LocalDateTime.now());
        Comment comment2 = new Comment(1L, "text2", item, author, LocalDateTime.now());
        List<Comment> commentList = List.of(comment1, comment2);
        List<CommentDto> commentDto = CommentMapper.commentDtoList(commentList);
        Assertions.assertNotNull(commentDto);
        assertEquals(commentDto.get(0).getText(), comment1.getText());
        assertEquals(commentDto.get(1).getText(), comment2.getText());
    }

    @Test
    public void testAddBookingAndComment() {
        // Создаем необходимые объекты
        User owner = new User(1L, "John", "john@example.com");
        Item item = Item.builder()
                .id(1L)
                .name("Laptop")
                .description("A high-performance laptop")
                .owner(owner)
                .build();
        List<Comment> comments = List.of(
                new Comment(1L, "Great laptop", item, owner, LocalDateTime.now()),
                new Comment(2L, "I love it", item, owner, LocalDateTime.now())
        );
        List<Booking> bookings = List.of(
                new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), item, owner, BookingStatus.APPROVED),
                new Booking(2L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, owner, BookingStatus.APPROVED)
        );
        LocalDateTime now = LocalDateTime.now();

        OutputItemDto result = itemService.addBookingAndComment(item, 1L, comments, bookings, now);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertNull(result.getAvailable());
        assertNull(result.getRequestId());
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertNotNull(result.getComments());
        assertEquals(comments.size(), result.getComments().size());
        assertEquals(comments.get(0).getId(), result.getComments().get(0).getId());
        assertEquals(comments.get(1).getId(), result.getComments().get(1).getId());
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

        List<Item> actualResult = itemRepository.searchItems(text, page);

        assertEquals(expectedResult, actualResult);
    }


//    @Test
//    public void testGetItems() {
//        Long authorId = 1L;
//        String searchText = "test";
//        int from = 0;
//        int size = 10;
//        Pageable page = PageRequest.of(from / size, size);
//        List<Item> itemList = Arrays.asList(
//                new Item(1L, "Test Item 1", "This is a test item 1", true, null, null),
//                new Item(2L, "Test Item 2", "This is a test item 2", true, null, null),
//                new Item(3L, "Another Item", "This is another item", true, null, null)
//        );
//        Mockito.when(itemRepository.searchItems(eq(searchText), eq(page))).thenReturn(itemList);
//
//        List<ItemDto> result = itemService.getItems(authorId, searchText, from, size);
//
//        assertNotNull(result);
//        assertEquals(3, result.size());
//        assertEquals(1L, result.get(0).getId().longValue());
//        assertEquals("Test Item 1", result.get(0).getName());
//        assertEquals("This is a test item 1", result.get(0).getDescription());
//        assertTrue(result.get(0).getAvailable());
//        assertNull(result.get(0).getRequestId());
//        assertEquals(2L, result.get(1).getId().longValue());
//        assertEquals("Test Item 2", result.get(1).getName());
//        assertEquals("This is a test item 2", result.get(1).getDescription());
//        assertTrue(result.get(1).getAvailable());
//        assertNull(result.get(1).getRequestId());
//    }

//    @Test
//    public void getItems_withBlankText_shouldReturnEmptyList() {
//        Long authorId = 1L;
//        String text = "";
//        int from = 0;
//        int size = 10;
//
//        List<ItemDto> actualResult = itemService.getItems(authorId, text, from, size);
//
//        assertTrue(actualResult.isEmpty());
//    }

    @Test
    public void testAddComment_Negative() {
        Long authorId = 1L;
        Long itemId = 3L;
        CommentDto commentDto = new CommentDto(1L, "This is a test comment", null, "z", LocalDateTime.now());
        assertThrows(NotFoundException.class, () -> itemService.addComment(authorId, itemId, commentDto));
    }

    @Test
    public void addComment_authorHasNotBookedItem_throwsIncorrectEntityParameterException() {
        Long authorId = 1L;
        Long itemId = 2L;
        CommentDto commentDto = new CommentDto();
        User user = new User();
        user.setId(authorId);
        Item item = new Item();
        item.setId(itemId);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findBookingsByItem(item, BookingStatus.APPROVED, authorId, LocalDateTime.now())).thenReturn(Collections.emptyList());


        assertThrows(IncorrectEntityParameterException.class, () -> {
            itemService.addComment(authorId, itemId, commentDto);
        });
    }

    @Test
    public void addComment_itemNotFound_throwsNotFoundException() {
        Long authorId = 1L;
        Long itemId = 2L;
        CommentDto commentDto = new CommentDto();
        User user = new User();
        user.setId(authorId);
        when(userRepository.findById(authorId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            itemService.addComment(authorId, itemId, commentDto);
        });
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



