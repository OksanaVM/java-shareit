package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item,Long> {
    List<Item> findAllByOwnerId(Long userId);

    List<Item> findByOwner(User owner);

    @Query(nativeQuery = true, value = "SELECT * FROM items WHERE is_available = true AND " +
            "(upper(name) like upper(concat('%', ?1, '%')) OR upper(description) like upper(concat('%', ?1, '%')))")
    List<Item> findAllItemsByLike(String text);
}
