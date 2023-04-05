package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemStorageImpl implements ItemStorage {
    private final UserStorage userStorage;

    private final Map<Long, Item> items = new HashMap<>();

    private Long counterId = 1L;

    @Override
    public List<Item> getItems(Long ownerId) {
        User user = userStorage.getUserById(ownerId);
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
    public Item addItem(Long ownerId, Item item) {
        item.setId(counterId);

        User user = userStorage.getUserById(ownerId);
        item.setOwner(user);

        items.put(counterId, item);

        counterId++;

        return item;
    }

    @Override
    public Item updateUser(Long ownerId, Long id, Item item) {
        User user = userStorage.getUserById(ownerId);

        Item newItem = items.get(id);
        if (newItem.getOwner().equals(user)) {
            if (item.getDescription() != null && !item.getDescription().isBlank()) {
                newItem.setDescription(item.getDescription());
            }

            if (item.getName() != null && !item.getName().isBlank()) {
                newItem.setName(item.getName());
            }

            if (item.getIsAvailable() != null) {
                newItem.setIsAvailable(item.getIsAvailable());
            }
            items.put(id, newItem);
        }
        return newItem;
    }
}
