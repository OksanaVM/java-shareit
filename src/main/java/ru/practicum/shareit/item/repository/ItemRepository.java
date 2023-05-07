package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwnerIdOrderById(Long userId, Pageable page);


    List<Item> findByRequestId(Long requestId);

    @Query(nativeQuery = true, value = "SELECT * FROM items WHERE is_available = true AND " +
            "(upper(name) like upper(concat('%', ?1, '%')) OR upper(description) like upper(concat('%', ?1, '%')))")
    List<Item> findAllItemsByLike(String text, Pageable page);
}
