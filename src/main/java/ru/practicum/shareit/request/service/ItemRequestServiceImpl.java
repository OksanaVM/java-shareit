package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
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
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public ItemRequestDto getById(long userId, long id) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        ItemRequest itemRequest = itemRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        List<Item> itemList = itemRepository.findByRequestId(itemRequestDto.getId());
        itemRequestDto.setItems(ItemMapper.toItemDtoList(itemList));

        return itemRequestDto;
    }

    @Override
    public List<ItemRequestDto> getAllUserRequest(Long userId) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<ItemRequest> itemRequestList = itemRequestRepository.findByRequestorOrderByCreated(requestor);
        List<ItemRequestDto> itemRequestDtoList = ItemRequestMapper.toItemRequestDtoList(itemRequestList);
        addItems(itemRequestDtoList);

        return itemRequestDtoList;
    }

//    private void addItems(List<ItemRequestDto> itemRequestDtoList) {
//        List<Long> requestIds = itemRequestDtoList.stream().map(ItemRequestDto::getId).collect(Collectors.toList());
//        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds).stream().collect(Collectors.groupingBy(Item::getRequestId));
//        itemRequestDtoList.forEach(requestDto -> {
//            List<Item> itemList = itemsByRequestId.getOrDefault(requestDto.getId(), Collections.emptyList());
//            requestDto.setItems(ItemMapper.toItemDtoList(itemList));
//        });
//    }
//
//    List<Item> findByRequestIdIn(List<Long> requestIds);

    @Override
    public List<ItemRequestDto> getAllRequest(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        PageRequest pageRequest = PageRequest.of(from / size, size, sort);
        List<ItemRequestDto> itemRequestDtoList = itemRequestRepository.findAllByRequestorIdNot(
                        userId,
                        pageRequest)
                .stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());

        addItems(itemRequestDtoList);
        return itemRequestDtoList;
    }

    private void addItems(List<ItemRequestDto> itemRequestDtoList) {
        List<Long> requestIds = itemRequestDtoList.stream().map(ItemRequestDto::getId).collect(Collectors.toList());
        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds).stream().collect(Collectors.groupingBy(Item::getRequestId));
        itemRequestDtoList.forEach(requestDto -> {
            List<Item> itemList = itemsByRequestId.getOrDefault(requestDto.getId(), Collections.emptyList());
            requestDto.setItems(ItemMapper.toItemDtoList(itemList));
        });
    }
}
