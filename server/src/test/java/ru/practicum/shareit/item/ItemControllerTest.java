package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.OutputItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.HeaderConstants;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc
public class ItemControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;


    @Test
    public void testCreateItem() throws Exception {
        ItemDto itemDto = new ItemDto(null, "test item", "test description", true, null);

        ItemDto createdItem = new ItemDto(1L, "test item", "test description", true, null);


        given(itemService.addItem(anyLong(), any(ItemDto.class))).willReturn(createdItem);

        mockMvc.perform(post("/items")
                        .header(HeaderConstants.OWNER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("test item")))
                .andExpect(jsonPath("$.description", is("test description")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    public void testGetItem() throws Exception {
        Long itemId = 1L;
        Long userId = 2L;

        OutputItemDto itemDto = OutputItemDto.builder()
                .id(itemId)
                .name("test item")
                .description("test description")
                .available(true)
                .build();

        given(itemService.getItem(itemId, userId)).willReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(HeaderConstants.OWNER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("test item")))
                .andExpect(jsonPath("$.description", is("test description")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    public void testGetItems() throws Exception {
        Long ownerId = 1L;
        Integer from = 0;
        Integer size = 10;

        List<OutputItemDto> items = List.of(
                OutputItemDto.builder()
                        .id(1L)
                        .name("test item 1")
                        .description("test description 1")
                        .available(true)
                        .build(),
                OutputItemDto.builder()
                        .id(2L)
                        .name("test item 2")
                        .description("test description 2")
                        .available(false)
                        .build()
        );

        given(itemService.getItems(ownerId, from, size)).willReturn(items);

        mockMvc.perform(get("/items")
                        .header(HeaderConstants.OWNER_ID, ownerId)
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("test item 1")))
                .andExpect(jsonPath("$[0].description", is("test description 1")))
                .andExpect(jsonPath("$[0].available", is(true)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("test item 2")))
                .andExpect(jsonPath("$[1].description", is("test description 2")))
                .andExpect(jsonPath("$[1].available", is(false)));
    }

//    @Test
//    public void testGetItemsList() throws Exception {
//        String text = "test";
//        Integer from = 0;
//        Integer size = 10;
//
//        List<ItemDto> items = List.of(
//                new ItemDto(1L, "test item 1", "test description 1", true, 123L),
//                new ItemDto(2L, "test item 2", "test description 2", false, 456L)
//        );
//
//        given(itemService.getItems(text, from, size)).willReturn(items);
//
//        mockMvc.perform(get("/items/search")
//                        .param("text", text)
//                        .param("from", "0")
//                        .param("size", "10")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id", is(1)))
//                .andExpect(jsonPath("$[0].name", is("test item 1")))
//                .andExpect(jsonPath("$[0].description", is("test description 1")))
//                .andExpect(jsonPath("$[0].available", is(true)))
//                .andExpect(jsonPath("$[0].requestId", is(123)))
//                .andExpect(jsonPath("$[1].id", is(2)))
//                .andExpect(jsonPath("$[1].name", is("test item 2")))
//                .andExpect(jsonPath("$[1].description", is("test description 2")))
//                .andExpect(jsonPath("$[1].available", is(false)))
//                .andExpect(jsonPath("$[1].requestId", is(456)));
//    }

    @Test
    public void testUpdateItem() throws Exception {
        Long ownerId = 123L;
        Long itemId = 1L;
        ItemDto itemDto = new ItemDto(itemId, "updated item", "updated description", true, null);

        given(itemService.update(ownerId, itemId, itemDto)).willReturn(itemDto);

        mockMvc.perform(patch("/items/{id}", itemId)
                        .header(HeaderConstants.OWNER_ID, ownerId)
                        .content(asJsonString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("updated item")))
                .andExpect(jsonPath("$.description", is("updated description")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.requestId", nullValue()));
    }


    @Test
    public void testAddComment() throws Exception {
        Long authorId = 123L;
        Long itemId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .text("Test comment")
                .build();
        CommentDto expectedComment = CommentDto.builder()
                .id(1L)
                .text("Test comment")
                .authorName("Test Author")
                .created(LocalDateTime.now())
                .build();

        given(itemService.addComment(authorId, itemId, commentDto)).willReturn(expectedComment);

        mockMvc.perform(post("/items/{id}/comment", itemId)
                        .header(HeaderConstants.OWNER_ID, authorId)
                        .content(asJsonString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Test comment")))
                .andExpect(jsonPath("$.authorName", is("Test Author")))
                .andExpect(jsonPath("$.created").isNotEmpty());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @Test
//    void addInvalidComment_shouldReturnStatus400() throws Exception {
//        CommentDto commentDto = CommentDto.builder().id(1L).text("testText").authorName("testName").build();
//
//        CommentDto invalidCommentDto = CommentDto.builder().id(1L).text("").authorName("testName").build();
//
//        when(itemService.addComment(any(), anyLong(), any()))
//                .thenReturn(commentDto);
//        mockMvc.perform(post("/items/1/comment")
//                        .header(HeaderConstants.OWNER_ID, 1L)
//                        .content(mapper.writeValueAsString(invalidCommentDto))
//                        .characterEncoding(StandardCharsets.UTF_8)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
//    }


}






