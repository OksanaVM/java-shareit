package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    List<Item> getItems(Long ownerId);

    List<Item> getItems(String text);

    Item getItem(Long id);

    Item addItem(Long ownerId, Item item);

    Item updateUser(Long ownerId, Long id, Item item);
}
