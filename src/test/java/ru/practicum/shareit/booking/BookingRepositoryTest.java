package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
public class BookingRepositoryTest {
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    protected TestEntityManager entityManager;

    public static User makeUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    public static Item makeItem(Long id, String name, String description, User user, boolean isAvailable) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setOwner(user);
        item.setIsAvailable(isAvailable);
        return item;
    }

    public static Booking makeBooking(
            Long id,
            LocalDateTime start,
            LocalDateTime end,
            Item item,
            User user,
            BookingStatus status
    ) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(status);
        return booking;
    }


    @Test
    public void shouldFindNoBookingsIfRepositoryIsEmpty() {
        Iterable<Booking> bookings = bookingRepository.findAll();

        assertThat(bookings).isEmpty();
    }

    @Test
    public void shouldStoreBooking() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        User owner = entityManager.persist(makeUser(null, "Пётр", "ivanov@mail.ru"));
        User booker = entityManager.persist(makeUser(null, "Иван", "petrov@mail.ru"));
        Item item = entityManager.persist(makeItem(null, "Итем", "Описание", owner, true));
        Booking booking = bookingRepository.save(makeBooking(null, start, end, item, booker, BookingStatus.WAITING));

        assertThat(booking)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING)
                .hasFieldOrProperty("item")
                .hasFieldOrProperty("booker");
        assertThat(booking.getItem())
                .isInstanceOf(Item.class)
                .hasFieldOrPropertyWithValue("name", "Итем");
    }

    @Test
    public void shouldCurrentByOwnerId() {
        LocalDateTime now = LocalDateTime.now();
        User owner = entityManager.persist(makeUser(null, "Пётр", "ivanov@mail.ru"));
        User booker = entityManager.persist(makeUser(null, "Иван", "petrov@mail.ru"));
        Item item1 = entityManager.persist(makeItem(null, "Итем", "Описание", owner, true));
        Item item2 = entityManager.persist(makeItem(null, "Итем2", "Описание2", owner, true));
        entityManager.persist(makeBooking(null, now.minusDays(1), now.plusDays(1), item1, booker, BookingStatus.WAITING));
        entityManager.persist(makeBooking(null, now.plusDays(1), now.plusDays(2), item2, booker, BookingStatus.WAITING));

        Sort sort = Sort.by("start_date").descending();
        Pageable pageable = PageRequest.of(0, 20, sort);
        List<Booking> listBookings = bookingRepository.findCurrentBookingsOwner(owner.getId(), LocalDateTime.now(), pageable);

        assertThat(listBookings)
                .hasSize(1)
                .element(0)
                .hasFieldOrProperty("item");
        assertThat(listBookings.get(0).getItem())
                .isInstanceOf(Item.class)
                .hasFieldOrPropertyWithValue("name", "Итем");
    }

    @Test
    public void shouldFindPastByOwner() {
        LocalDateTime now = LocalDateTime.now();
        User owner1 = entityManager.persist(makeUser(null, "Пётр", "ivanov@mail.ru"));
        User owner2 = entityManager.persist(makeUser(null, "Иван", "petrov@mail.ru"));
        Item item1 = entityManager.persist(makeItem(null, "Итем1", "Описание1", owner1, true));
        Item item2 = entityManager.persist(makeItem(null, "Итем2", "Описание2", owner2, true));
        entityManager.persist(makeBooking(null, now.minusDays(2), now.minusDays(1), item1, owner1, BookingStatus.WAITING));
        entityManager.persist(makeBooking(null, now.minusDays(3), now.minusDays(2), item2, owner2, BookingStatus.WAITING));

        Sort sort = Sort.by("end_date").descending();
        Pageable pageable = PageRequest.of(0, 20, sort);
        List<Booking> listBookings = bookingRepository.findPastByOwner(owner1.getId(), LocalDateTime.now(), pageable);

        assertThat(listBookings)
                .hasSize(1)
                .element(0)
                .hasFieldOrProperty("item");
        assertThat(listBookings.get(0).getItem())
                .isInstanceOf(Item.class)
                .hasFieldOrPropertyWithValue("name", "Итем1");
    }

    @Test
    public void shouldFindFutureByOwnerId() {
        LocalDateTime now = LocalDateTime.now();
        User owner = entityManager.persist(makeUser(null, "Пётр", "ivanov@mail.ru"));
        User booker = entityManager.persist(makeUser(null, "Иван", "petrov@mail.ru"));
        Item item1 = entityManager.persist(makeItem(null, "Итем", "Описание", owner, true));
        Item item2 = entityManager.persist(makeItem(null, "Итем2", "Описание2", owner, true));
        entityManager.persist(makeBooking(null, now.minusDays(2), now.minusDays(1), item1, booker, BookingStatus.WAITING));
        entityManager.persist(makeBooking(null, now.plusDays(1), now.plusDays(2), item2, booker, BookingStatus.WAITING));

        Sort sort = Sort.by("start_date").descending();
        Pageable pageable = PageRequest.of(0, 20, sort);
        List<Booking> listBookings = bookingRepository.findFutureByOwner(owner.getId(), LocalDateTime.now(), pageable);

        assertThat(listBookings)
                .hasSize(1)
                .element(0)
                .hasFieldOrProperty("item");
        assertThat(listBookings.get(0).getItem())
                .isInstanceOf(Item.class)
                .hasFieldOrPropertyWithValue("name", "Итем2");
    }


}
