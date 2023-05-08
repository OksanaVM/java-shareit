package ru.practicum.shareit.request.mapper;

import lombok.NonNull;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                UserMapper.toUserDto(itemRequest.getRequestor()),
                itemRequest.getCreated(),
                null);
    }


    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User requestor) {
        return new ItemRequest(
                itemRequestDto.getId(),
                itemRequestDto.getDescription(),
                UserMapper.toUserModel(itemRequestDto.getRequestor()),
                itemRequestDto.getCreated()
        );
    }

    public static List<ItemRequestDto> toDtos(@NonNull List<ItemRequest> requests) {
        List<ItemRequestDto> dtos = new ArrayList<>();
        requests.forEach(request -> dtos.add(toItemRequestDto(request)));
        return dtos;
    }

    public static List<ItemRequestDto> toItemRequestDtoList(List<ItemRequest> itemRequestList) {
        return itemRequestList.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }


}
