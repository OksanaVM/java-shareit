package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.dto.ItemBookingInfoDto;

import java.util.List;


@AllArgsConstructor
@Setter
@Getter
@Builder
public class ItemsDto {
    private final Long id;
    private final String name;
    private final String description;
    private final Boolean available;
    private final Long requestId;
    private final ItemBookingInfoDto lastBooking;
    private final ItemBookingInfoDto nextBooking;
    private final List<CommentDto> comments;
}
