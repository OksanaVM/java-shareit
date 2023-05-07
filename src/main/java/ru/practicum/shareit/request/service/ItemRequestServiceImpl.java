package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.IncorrectEntityParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImpl implements ItemRequestService{

    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;


    @Override
    public ItemRequestDto create(long userId, ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = new ItemRequest();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        itemRequest.setRequestor(user);

        itemRequest.setCreated(LocalDateTime.now());
        if (itemRequestDto.getDescription() == null) {
            throw new NotFoundException("Описание не может быть пустым");
        }
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long id) {
         userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Optional<ItemRequest> itemRequestOptional = itemRequestRepository.findById(id);
        if (itemRequestOptional.isPresent()) {
            ItemRequest itemRequest = itemRequestOptional.get();

            ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
            List<Item> itemList = itemRepository.findByRequestId(itemRequestDto.getId());
            itemRequestDto.setItems(ItemMapper.toItemDtoList(itemList));

            return itemRequestDto;
        } else {
            throw new IncorrectEntityParameterException("Запрос не найден");
        }
    }

    @Override
    public List<ItemRequestDto> getAllUserRequest(Long userId) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<ItemRequest> itemRequestList = itemRequestRepository.findByRequestor(requestor);
        List<ItemRequestDto> itemRequestDtoList = ItemRequestMapper.toItemRequestDtoList(itemRequestList);
        addItems(itemRequestDtoList);

        itemRequestDtoList = sortItemRequestList(itemRequestDtoList);

        return itemRequestDtoList;
    }

    // получить список ВCЕХ запросов, созданных другими пользователями
    @Override
    public List<ItemRequestDto> getAllRequest(Long userId, Integer from, Integer size) {
        List<ItemRequest> itemRequestList;
        if (from == null || size == null) {
            itemRequestList = itemRequestRepository.findAll();
            return ItemRequestMapper.toItemRequestDtoList(itemRequestList);
        } else if (from < 0 || size <= 0) {
            throw new IncorrectEntityParameterException("Неверные параметры");
        } else {
            User requestor = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
            itemRequestList = itemRequestRepository.findByRequestorLimits(requestor.getId(), from, size);
            List<ItemRequestDto> itemRequestDtoList = ItemRequestMapper.toItemRequestDtoList(itemRequestList);
            addItems(itemRequestDtoList);
            itemRequestDtoList = sortItemRequestList(itemRequestDtoList);
            return itemRequestDtoList;
        }
    }


    private void addItems(List<ItemRequestDto> itemRequestDtoList) {
        itemRequestDtoList.forEach(requestDto -> {
            List<Item> itemList = itemRepository.findByRequestId(requestDto.getId());
            requestDto.setItems(ItemMapper.toItemDtoList(itemList));
        });
    }


    private List<ItemRequestDto> sortItemRequestList(List<ItemRequestDto> itemRequestDtoList) {
        itemRequestDtoList = itemRequestDtoList.stream()
                .sorted(Comparator.comparing(ItemRequestDto::getCreated))
                .collect(Collectors.toList());
        return itemRequestDtoList;
    }
}
