package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemStorageImpl implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();

    private Long counterId = 1L;

    @Override
    public List<Item> getItems(User user) {
        return items.values().stream()
                .filter(item -> item.getOwner().equals(user))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> getItems(String text) {
        return items.values().stream()
                .filter(item -> item.getIsAvailable()
                        && !text.isBlank()
                        && (item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                ).collect(Collectors.toList());
    }

    @Override
    public Item getItem(Long id) {
        return items.get(id);
    }

    @Override
    public Item addItem(Item item) {
        item.setId(counterId);
        items.put(counterId, item);
        counterId++;

        return item;
    }

    @Override
    public Item updateItem(Item item) {
        Item newItem = items.get(item.getId());
        if (newItem.getOwner().equals(item.getOwner())) {
            if (item.getDescription() != null && !item.getDescription().isBlank()) {
                newItem.setDescription(item.getDescription());
            }

            if (item.getName() != null && !item.getName().isBlank()) {
                newItem.setName(item.getName());
            }

            if (item.getIsAvailable() != null) {
                newItem.setIsAvailable(item.getIsAvailable());
            }
            items.put(item.getId(), newItem);
        }
        return newItem;
    }
}
