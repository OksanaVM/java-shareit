package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemStorage {
    List<Item> getItems(User user);

    List<Item> getItems(String text);

    Item getItem(Long id);

    Item addItem(Item item);

    Item updateItem(Item item);
}
