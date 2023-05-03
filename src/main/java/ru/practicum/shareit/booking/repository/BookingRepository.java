package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;


public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByItemAndBooker(Item item, User user);

    @Query(nativeQuery = true, value = "select * from bookings " +
            "where item_id = :itemId AND status != 'REJECTED' " +
            "order by start_date desc")
    List<Booking> findAllBookingsItem(@Param("itemId") Long itemId);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 order by b.start desc ")
    List<Booking> findAllByOwnerItems(@Param("owner") long ownerId);


    @Query(nativeQuery = true, value = "select * from bookings b " +
            "inner join items i on b.item_id = i.id " +
            "where i.owner_id = :ownerId " +
            "and :time between b.start_date and b.end_date " +
            "order by b.start_date desc")
    List<Booking> findAllCurrentBookingsOwner(Long ownerId, LocalDateTime time);

    @Query(nativeQuery = true, value = "select * from bookings b " +
            "inner join items i on b.item_id = i.id " +
            "where i.owner_id = :ownerId " +
            "and b.end_date < :time " +
            "order by b.start_date desc")
    List<Booking> findAllPastBookingsOwner(Long ownerId, LocalDateTime time);

    @Query(nativeQuery = true, value = "select * from bookings b " +
            "inner join items i on b.item_id = i.id " +
            "where i.owner_id = :ownerId " +
            "and b.start_date > :time " +
            "order by b.start_date desc")
    List<Booking> findAllFutureBookingsOwner(Long ownerId, LocalDateTime time);

    @Query("SELECT b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "AND b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findByOwnerAndState(long userId, BookingStatus status);

    List<Booking> findAllByBooker_IdOrderByStartDesc(long id);

    List<Booking> findAllByBooker_IdAndStatusIsOrderByStartDesc(Long id, BookingStatus status);

    List<Booking> findAllByBooker_IdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(Long id,
                                                                                  LocalDateTime end,
                                                                                  LocalDateTime start);

    List<Booking> findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(Long id, LocalDateTime time);

    List<Booking> findAllByBooker_IdAndStartIsAfterOrderByStartDesc(Long id, LocalDateTime time);

    List<Booking> findAllByBooker_IdAndStartIsAfterAndStatusIsOrderByStartDesc(Long bookerId,
                                                                               LocalDateTime start,
                                                                               BookingStatus status);

    List<Booking> findByItemInAndStatus(List<Item> items, BookingStatus status, Sort created);

}

