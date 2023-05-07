package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findByRequestor(User user);

    @Query(nativeQuery = true, value = "SELECT * from (SELECT b.*, ROWNUM() r FROM (SELECT b.* FROM REQUESTS b WHERE b.requestor_id<>:user ORDER BY created DESC ) b ) " +
            "WHERE r>:from and ROWNUM<=:size ORDER BY created DESC")
    List<ItemRequest> findByRequestorLimits(@Param("user") Long userId, @Param("from") Integer from, @Param("size") Integer size);
}


