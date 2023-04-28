package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemsDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long ownerId, ItemDto itemDto);

    ItemDto update(Long ownerId, Long itemId, ItemDto itemDto);

    ItemsDto getItem(Long itemId, Long userId);

    List<ItemsDto> getItems(Long ownerId);

    List<ItemDto> getItems(String text);

    CommentDto addComment(Long authorId, Long itemId, CommentDto commentDto);
}
