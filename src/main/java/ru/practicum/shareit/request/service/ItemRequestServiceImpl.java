package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
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


    @Transactional(readOnly = true)
    @Override
    public ItemRequestDto getById(long userId, long id) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        ItemRequest itemRequest = itemRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        List<Item> itemList = itemRepository.findByRequestId(itemRequestDto.getId());
        itemRequestDto.setItems(ItemMapper.toItemDtoList(itemList));

        return itemRequestDto;
    }

    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
