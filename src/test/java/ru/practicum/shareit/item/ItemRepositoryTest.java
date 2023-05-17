package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;

    @Autowired
    protected TestEntityManager entityManager;

    private final User user = new User(
            null,
            "Igor",
            "igor@gmail.dom");

    private final Item item = new Item(
            null,
            "Какая-то вещь",
            "Какое-то описание",
            true,
            1L,
            user);

    private final User requestor = User.builder()
            .name("John Doe")
            .email("john.doe@example.com")
            .build();
    private final ItemRequest request = new ItemRequest(
            null,
            "Запрос",
            user,
            LocalDateTime.now());


    @BeforeEach
    void setup() {
        entityManager.persist(user);
        entityManager.persist(request);
        entityManager.flush();
    }

    @Test
    public void testFindAllItemsByLike_withBlankText_shouldReturnEmptyList() {
        String text = "";
        Pageable page = PageRequest.of(0, 10);
        List<Item> expectedResult = Collections.emptyList();

        List<Item> actualResult = itemRepository.searchItems(text, page);

        assertEquals(expectedResult, actualResult);
    }

    @Test
    void createItem() {
        Item found = itemRepository.save(item);

        Assertions.assertNotNull(found);
        Assertions.assertEquals(1L, found.getId());
        Assertions.assertEquals(item.getName(), found.getName());
        Assertions.assertEquals(item.getDescription(), found.getDescription());
        Assertions.assertTrue(found.getIsAvailable());
        Assertions.assertNotNull(found.getRequestId());
        Assertions.assertEquals(user.getName(), found.getOwner().getName());
        Assertions.assertEquals(user.getEmail(), found.getOwner().getEmail());
    }

    @Test
    void findItemById() {
        entityManager.persist(item);
        entityManager.flush();

        Item found = itemRepository.findById(1L).orElse(null);

        Assertions.assertNotNull(found);
        Assertions.assertEquals(1L, found.getId());
        Assertions.assertEquals(item.getName(), found.getName());
        Assertions.assertEquals(item.getDescription(), found.getDescription());
        Assertions.assertTrue(found.getIsAvailable());
        Assertions.assertNotNull(found.getRequestId());
        Assertions.assertEquals(user.getName(), found.getOwner().getName());
        Assertions.assertEquals(user.getEmail(), found.getOwner().getEmail());
    }

    @Test
    void findAllByOwnerIdOrderByIdAsc() {
        entityManager.persist(item);
        entityManager.flush();

        List<Item> found = itemRepository.findByOwnerIdOrderById(1L, PageRequest.of(0, 1));

        Assertions.assertNotNull(found);
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(1L, found.get(0).getId());
        Assertions.assertEquals(item.getName(), found.get(0).getName());
        Assertions.assertEquals(item.getDescription(), found.get(0).getDescription());
        Assertions.assertTrue(found.get(0).getIsAvailable());
        Assertions.assertNotNull(found.get(0).getRequestId());
        Assertions.assertEquals(user.getName(), found.get(0).getOwner().getName());
        Assertions.assertEquals(user.getEmail(), found.get(0).getOwner().getEmail());
    }

    @Test
    void findAllByRequestIdOrderByIdAsc() {
        entityManager.persist(item);
        entityManager.flush();

        List<Item> found = itemRepository.findByRequestId(1L);

        Assertions.assertNotNull(found);
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(1L, found.get(0).getId());
        Assertions.assertEquals(item.getName(), found.get(0).getName());
        Assertions.assertEquals(item.getDescription(), found.get(0).getDescription());
        Assertions.assertTrue(found.get(0).getIsAvailable());
        Assertions.assertNotNull(found.get(0).getRequestId());
        Assertions.assertEquals(user.getName(), found.get(0).getOwner().getName());
        Assertions.assertEquals(user.getEmail(), found.get(0).getOwner().getEmail());
    }

    @Test
    void search() {
        entityManager.persist(item);
        entityManager.flush();

        List<Item> found = itemRepository.searchItems("Какая-то вещь", PageRequest.of(0, 1));

        Assertions.assertNotNull(found);
        Assertions.assertEquals(1, found.size());
        Assertions.assertEquals(1L, found.get(0).getId());
        Assertions.assertEquals(item.getName(), found.get(0).getName());
        Assertions.assertEquals(item.getDescription(), found.get(0).getDescription());
        Assertions.assertTrue(found.get(0).getIsAvailable());
        Assertions.assertNotNull(found.get(0).getRequestId());
        Assertions.assertEquals(user.getName(), found.get(0).getOwner().getName());
        Assertions.assertEquals(user.getEmail(), found.get(0).getOwner().getEmail());
    }

}
