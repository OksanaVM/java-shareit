package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.IncorrectEntityParameterException;
import ru.practicum.shareit.item.dto.AuthorDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTest {

    private BookingRepository mockBookingRepository;
    private UserRepository mockUserRepository;
    private ItemRepository mockItemRepository;
    private CommentRepository mockCommentRepository;


    private final User owner = new User(1L, "eee@email.ru", "Eva");
    private final Item item = new Item(1L, "carpet", "description", true, null, owner);

    private final User booker = new User(2L, "ppp@email.ru", "Polina");

    private final ItemService itemService = new ItemServiceImpl(mockUserRepository, mockItemRepository, mockBookingRepository, mockCommentRepository);


    @Test
    public void shouldFailAddItemWithIncorrectParam() {
        ItemDto newItem = new ItemDto(null, null, null, null, null);
        IncorrectEntityParameterException exception = assertThrows(IncorrectEntityParameterException.class, () -> itemService.addItem(owner.getId(), newItem));
        Assertions.assertNotNull(exception);

        ItemDto newItemWithoutName = new ItemDto(null, null, null, true, null);
        exception = assertThrows(IncorrectEntityParameterException.class, () -> itemService.addItem(owner.getId(), newItemWithoutName));
        Assertions.assertNotNull(exception);
        assertEquals(exception.getParameter(), "Название не может быть пустой");

        ItemDto newItemWithoutDescription = new ItemDto(null, "name", null, true, null);
        exception = assertThrows(IncorrectEntityParameterException.class, () -> itemService.addItem(owner.getId(), newItemWithoutDescription));
        Assertions.assertNotNull(exception, "Описание не может быть пустой");
    }

    @Test
    public void testAddItemNullOwnerId() {
        // Создаем объект itemDto
        ItemDto itemDto = new ItemDto(null, "Laptop", "A high-performance laptop", true, null);

        // Проверяем, что метод выбросит исключение
        Exception exception = assertThrows(IncorrectEntityParameterException.class, () -> {
            itemService.addItem(null, itemDto);
        });

        // Проверяем сообщение исключения
        String expectedMessage = "Owner ID не может быть null";
        Assertions.assertNotNull(exception, expectedMessage);
    }

    @Test
    public void shouldMapToCommentDtoList() {
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
    public void testId() {
        AuthorDto authorDto = new AuthorDto(1L, "John Doe", "john@example.com");
        assertEquals(1L, authorDto.getId());
    }

    @Test
    public void testAuthorName() {
        AuthorDto authorDto = new AuthorDto(1L, "John Doe", "john@example.com");
        assertEquals("John Doe", authorDto.getAuthorName());
    }

    @Test
    public void testEmail() {
        AuthorDto authorDto = new AuthorDto(1L, "John Doe", "john@example.com");
        assertEquals("john@example.com", authorDto.getEmail());
    }

    @Test
    public void testNoArgsConstructor() {
        AuthorDto authorDto = new AuthorDto();
        assertNull(authorDto.getId());
        assertNull(authorDto.getAuthorName());
        assertNull(authorDto.getEmail());
    }

    @Test
    public void testAllArgsConstructor() {
        AuthorDto authorDto = new AuthorDto(1L, "John Doe", "john@example.com");
        assertEquals(1L, authorDto.getId());
        assertEquals("John Doe", authorDto.getAuthorName());
        assertEquals("john@example.com", authorDto.getEmail());
    }

}
