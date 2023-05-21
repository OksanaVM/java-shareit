package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ItemRequestClient itemRequestClient;

    @Test
    void addRequest() throws Exception {
        ItemRequestDto itemRequestDto = getItemRequestDto();
        String itemRequestJson = objectMapper.writeValueAsString(itemRequestDto);
        ResponseEntity<Object> response = new ResponseEntity<>(itemRequestJson, HttpStatus.OK);
        when(itemRequestClient.addRequest(anyLong(), any())).thenReturn(response);
        String content = mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(objectMapper.writeValueAsString(itemRequestDto), content);
    }

    @Test
    void getRequestsByRequester() throws Exception {
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(itemRequestClient).getAllUserRequest(1L);
    }

    @Test
    void getRequestById() throws Exception {
        mockMvc.perform(get("/requests/{requestId}", 1)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(itemRequestClient).getById(1L, 1L);
    }

    @Test
    void getAllRequests() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(itemRequestClient).getAllRequest(1L, 0, 10);
    }

    @Test
    void getAllRequestsByPage() throws Exception {
        mockMvc.perform(get("/requests/all?from={from}&size={size}", 5, 5)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(itemRequestClient).getAllRequest(1L, 5, 5);
    }

    @Test
    void getAllRequestsByPageNegative() throws Exception {
        mockMvc.perform(get("/requests/all?from={from}&size={size}", 5, -5)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isBadRequest());
        Mockito.verify(itemRequestClient, Mockito.never()).getAllRequest(1L, 5, 5);
    }

    ItemRequestDto getItemRequestDto() {
        return new ItemRequestDto(null,
                "request"
        );
    }
}
