package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.IncorrectItemParameterException;
import ru.practicum.shareit.item.exception.IncorrectOwnerParameterException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
        if (ownerId == null) {
            throw new IncorrectItemParameterException("Owner ID не может быть null");
        }
        checkOwner(ownerId);
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new IncorrectItemParameterException("Название не может быть пустой");
        } else if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new IncorrectItemParameterException("Описание не может быть пустой");
        } else if (itemDto.getAvailable() == null) {
            throw new IncorrectItemParameterException("Статус не может быть пустой");
        } else {
            Item item = ItemMapper.toItem(itemDto);
            User user = userStorage.getUserById(ownerId);
            item.setOwner(user);
            Item newItem = itemStorage.addItem(item);

            return ItemMapper.toItemDto(newItem);
        }
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        checkOwner(ownerId);

        Item oldItem = itemStorage.getItem(itemId);
        if (oldItem.getOwner().getId().equals(ownerId)) {
            Item item = ItemMapper.toItem(itemDto);
            User user = userStorage.getUserById(ownerId);
            item.setOwner(user);
            item.setId(itemId);
            Item newItem = itemStorage.updateUser(item);
            return ItemMapper.toItemDto(newItem);
        } else {
            throw new IncorrectOwnerParameterException("Пользователь не найден");
        }
    }

    @Override
    public ItemDto getItem(Long itemId) {
        Item newItem = itemStorage.getItem(itemId);
        return ItemMapper.toItemDto(newItem);
    }

    @Override
    public List<ItemDto> getItems(Long ownerId) {
        checkOwner(ownerId);
        User user = userStorage.getUserById(ownerId);
        return ItemMapper.toItemDtoList(itemStorage.getItems(user));
    }

    @Override
    public List<ItemDto> getItems(String text) {
        return ItemMapper.toItemDtoList(itemStorage.getItems(text));
    }

    private void checkOwner(Long ownerId) {
        User user = userStorage.getUserById(ownerId);
        if (user == null) {
            throw new IncorrectOwnerParameterException("Пользователь не найден");
        }
    }
}
