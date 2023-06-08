package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ItemClient itemClient;

    @Test
    void addItem() throws Exception {
        int userId = 1;
        ItemDto itemDto = getItemDto("name item");
        String itemJson = objectMapper.writeValueAsString(itemDto);
        ResponseEntity<Object> response = new ResponseEntity<>(itemJson, HttpStatus.OK);
        when(itemClient.postItem(ArgumentMatchers.any(), ArgumentMatchers.anyLong())).thenReturn(response);
        String content = mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getItemDto("name item")))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertEquals(objectMapper.writeValueAsString(getItemDto("name item")), content);
    }

    @Test
    void addItemBad() throws Exception {
        int userId = 1;
        ItemDto itemDto = getItemDto("");
        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Mockito.verify(itemClient, Mockito.never()).postItem(ArgumentMatchers.any(), ArgumentMatchers.anyLong());
    }

    @Test
    void pathItem() throws Exception {
        int userId = 1;
        int itemId = 1;
        ItemDto itemDto = getItemDto("name item");
        String itemJson = objectMapper.writeValueAsString(itemDto);
        ResponseEntity<Object> response = new ResponseEntity<>(itemJson, HttpStatus.OK);
        Mockito.when(itemClient.patchItem(ArgumentMatchers.any(), ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong()))
                .thenReturn(response);
        String content = mockMvc.perform(MockMvcRequestBuilders.patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(objectMapper.writeValueAsString(itemDto), content);
    }

    @Test
    void getItem() throws Exception {
        long itemId = 1L;
        long userId = 1L;
        mockMvc.perform(MockMvcRequestBuilders.get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(itemClient).getItemById(itemId, userId);
    }

    @Test
    void getItemNotFound() throws Exception {
        long itemId = 99;
        long userId = 1;
        mockMvc.perform(MockMvcRequestBuilders.get("/items/{itemId}", itemId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(itemClient, Mockito.never()).getItemById(itemId, userId);
    }

    @Test
    void getItemsByOwner() throws Exception {
        long userId = 1;
        int from = 1;
        int size = 10;
        mockMvc.perform(MockMvcRequestBuilders.get("/items?from={from}&size={size}", from, size)
                        .header("X-Sharer-User-Id", userId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(itemClient).getAllUsersItems(userId, 1, 10);
    }

    @Test
    void getItemsByOwnerNegative() throws Exception {
        long userId = 1;
        int from = -1;
        int size = -10;
        mockMvc.perform(MockMvcRequestBuilders.get("/items?from={from}&size={size}", from, size)
                        .header("X-Sharer-User-Id", userId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(itemClient, Mockito.never()).getAllUsersItems(userId, 1, 10);
    }


    @Test
    void addComment() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        CommentDto commentDto = new CommentDto(1L, "comment text", null, "user");
        String commentJson = objectMapper.writeValueAsString(commentDto);
        ResponseEntity<Object> response = new ResponseEntity<>(commentJson, HttpStatus.OK);
        Mockito.when(itemClient.addComment(ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong(), ArgumentMatchers.any())).thenReturn(response);
        String content = mockMvc.perform(MockMvcRequestBuilders.post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(objectMapper.writeValueAsString(commentDto), content);
    }

    ItemDto getItemDto(String name) {
        return new ItemDto(
                1L,
                name,
                "description",
                false,
                null
        );
    }
}
