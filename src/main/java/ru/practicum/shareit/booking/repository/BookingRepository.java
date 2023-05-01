package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;


public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBooker(User user);

    @Query(nativeQuery = true, value = "SELECT * from (SELECT b.*, ROWNUM() r FROM (SELECT b.* FROM booking b WHERE b.USER_ID=:user ORDER BY DATE_BEGIN DESC ) b) " +
            "WHERE r>:from and ROWNUM<=:size ORDER BY DATE_BEGIN DESC")
    List<Booking> findByBookerByPage(@Param("user") Integer userId, @Param("from") Integer from, @Param("size") Integer size);

    List<Booking> findByItem(Item item);

    @Query(nativeQuery = true, value = "SELECT * from (SELECT b.*, ROWNUM() r FROM (SELECT b.* FROM booking b WHERE b.ITEM_ID=:item ORDER BY DATE_BEGIN DESC ) b ) " +
            "WHERE r>:from and ROWNUM<=:size ORDER BY DATE_BEGIN DESC")
    List<Booking> findByItemByLimits(@Param("item") Long item, @Param("from") Integer from, @Param("size") Integer size);

    List<Booking> findByItemAndBooker(Item item, User user);

    @Query(nativeQuery = true, value = "select * from bookings " +
            "where item_id = :itemId AND status != 'REJECTED' " +
            "order by start_date desc")
    List<Booking> findAllBookingsItem(@Param("itemId") Long itemId);
}

