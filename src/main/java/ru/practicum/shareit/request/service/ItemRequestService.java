package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(long userId, ItemRequestDto itemRequestDto);

    ItemRequestDto getById(long userId, long id);

    List<ItemRequestDto> getAllUserRequest(Long userId);

    List<ItemRequestDto> getAllRequest(Long userId, Integer from, Integer size);
}
