package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserClient userClient;


    @Test
    void addUser() throws Exception {
        UserDto userDto = new UserDto(1L, "name", "mail@mail.m");
        String userJson = objectMapper.writeValueAsString(userDto);
        ResponseEntity<Object> response = new ResponseEntity<>(userJson, HttpStatus.OK);
        when(userClient.postUser(any())).thenReturn(response);
        String result = mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(objectMapper.writeValueAsString(userDto), result);
    }

    @Test
    void addUserInvalidEmail() throws Exception {
        UserDto userDto = new UserDto(1L, "name", "mail");
        String userJson = objectMapper.writeValueAsString(userDto);
        ResponseEntity<Object> response = new ResponseEntity<>(userJson, HttpStatus.OK);
        when(userClient.postUser(any())).thenReturn(response);
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(userClient, never()).postUser(any());
        userJson = objectMapper.writeValueAsString(new UserDto(null, "nameNew", null));
        response = new ResponseEntity<>(userJson, HttpStatus.OK);
        when(userClient.postUser(any())).thenReturn(response);
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        verify(userClient, never()).postUser(any());
    }

    @Test
    void pathUser() throws Exception {
        UserDto userDto = new UserDto(1L, "name", "mail@mail.m");
        UserDto userDtoUpdate = new UserDto(1L, "nameNew", "mail@mail.m");
        String userJson = objectMapper.writeValueAsString(userDtoUpdate);
        ResponseEntity<Object> response = new ResponseEntity<>(userJson, HttpStatus.OK);
        when(userClient.patchUser(any(), anyLong())).thenReturn(response);
        String result = mockMvc.perform(
                        patch("/users/{userId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertEquals(objectMapper.writeValueAsString(userDtoUpdate), result);
    }

    @Test
    void getUser() throws Exception {
        int userId = 1;
        mockMvc.perform(get("/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userClient).getUser(userId);
    }

    @Test
    void getAllUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userClient).getAllUsers();
    }

    @Test
    void deleteUser() throws Exception {
        int userId = 1;
        mockMvc.perform(delete("/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userClient).delete(userId);
    }

}
