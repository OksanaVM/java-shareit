package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;


@Data
@AllArgsConstructor
@ToString
@Builder
public class ItemDto {
    private final Long id;
    private final String name;
    private final String description;
    private final Boolean available;
    private Long requestId;

}
