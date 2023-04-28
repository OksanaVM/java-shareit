package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ItemBookingInfoDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setBooker(UserMapper.toUserDto(booking.getBooker()));
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setItem(ItemMapper.toItemDto(booking.getItem()));
        dto.setStatus(booking.getStatus());
        return dto;
    }


    public static List<BookingDto> toBookingDtoList(List<Booking> bookingList) {
        return bookingList.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public static ItemBookingInfoDto toItemBookingInfoDto(Booking booking) {
        return new ItemBookingInfoDto(booking.getId(), booking.getBooker().getId(),
                booking.getStart(), booking.getEnd());
    }
}




