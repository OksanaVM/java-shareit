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
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    BookingClient bookingClient;

    @Test
    void addBooking() throws Exception {
        BookItemRequestDto bookingDto = getBookingDto(LocalDateTime.now().plusDays(2));
        String bookingJson = objectMapper.writeValueAsString(bookingDto);
        ResponseEntity<Object> response = new ResponseEntity<>(bookingJson, HttpStatus.OK);
        when(bookingClient.bookItem(ArgumentMatchers.anyLong(), ArgumentMatchers.any()))
                .thenReturn(response);
        String content = mockMvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getBookingDto(LocalDateTime.now().plusDays(2))))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(bookingJson, content);
    }

    @Test
    void addBookingBad() throws Exception {
        BookItemRequestDto bookingDto = getBookingDto(LocalDateTime.now().minusDays(3));
        mockMvc.perform(MockMvcRequestBuilders.post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Mockito.verify(bookingClient, Mockito.never()).bookItem(ArgumentMatchers.anyInt(), ArgumentMatchers.any());
    }

    @Test
    void getBooking() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(bookingClient).getBooking(1, 1L);
    }

    @Test
    void confirmBooking() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/bookings/{bookingId}?approved={approved}", 1, true)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(bookingClient).approve(1L, 1L, true);
    }

    @Test
    void getBookingByOwner() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(bookingClient).ownerItemsBookingLists(1L, BookingState.valueOf("ALL"), 0, 10);

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings/owner?state={state}", "WAITING")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(bookingClient).ownerItemsBookingLists(1L, BookingState.valueOf("WAITING"), 0, 10);

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings/owner?from={from}&size={size}", -1, 0)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isBadRequest());

        Mockito.verify(bookingClient, Mockito.never()).ownerItemsBookingLists(1L, BookingState.valueOf("ALL"), -1, 0);
    }

    @Test
    void getBookingByBooker() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/bookings")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(bookingClient).getBookings(1, BookingState.valueOf("ALL"), 0, 10);

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings?state={state}", "WAITING")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
        Mockito.verify(bookingClient).getBookings(1, BookingState.valueOf("WAITING"), 0, 10);

        mockMvc.perform(MockMvcRequestBuilders.get("/bookings?from={from}&size={size}", -1, 0)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isBadRequest());
        Mockito.verify(bookingClient, Mockito.never()).getBookings(1, BookingState.valueOf("ALL"), -1, 0);
    }

    BookItemRequestDto getBookingDto(LocalDateTime end) {
        return new BookItemRequestDto(1,
                LocalDateTime.now().plusDays(1),
                end
        );
    }

}
