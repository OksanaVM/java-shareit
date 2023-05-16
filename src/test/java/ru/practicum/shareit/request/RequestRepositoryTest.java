package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;


@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RequestRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRequestRepository requestRepository;

    private final User user = new User(
            null,
            "Oksana",
            "Oksana@gmail.dom");

    private final User anotherUser = new User(
            null,
            "Vasia",
            "Vasia@gmail.dom");

    private final ItemRequest request = new ItemRequest(
            null,
            "Запрос",
            user,
            LocalDateTime.now());

    private final ItemRequest anotherRequest = new ItemRequest(
            null,
            "Другой запрос",
            anotherUser,
            LocalDateTime.now().plusDays(1));

    @BeforeEach
    void setup() {
        entityManager.persist(user);
        entityManager.persist(anotherUser);
        entityManager.flush();
    }

    @Test
    void createRequest() {
        ItemRequest createdRequest = requestRepository.save(request);

        Assertions.assertNotNull(createdRequest);
        Assertions.assertEquals(1L, createdRequest.getId());
        Assertions.assertEquals(request.getDescription(), createdRequest.getDescription());
        Assertions.assertEquals(request.getRequestor(), createdRequest.getRequestor());
        Assertions.assertEquals(request.getCreated(), createdRequest.getCreated());
    }

    @Test
    void findRequestById() {
        entityManager.persist(request);
        entityManager.flush();

        ItemRequest found = requestRepository.findById(1L).orElse(null);

        Assertions.assertNotNull(found);
        Assertions.assertEquals(1L, found.getId());
        Assertions.assertEquals(request.getDescription(), found.getDescription());
        Assertions.assertEquals(request.getRequestor(), found.getRequestor());
        Assertions.assertEquals(request.getCreated(), found.getCreated());
    }

    @Test
    void findAllByRequestorIdNotOrderByCreatedAsc() {
        entityManager.persist(request);
        entityManager.persist(anotherRequest);
        entityManager.flush();

        Long user2Id = 2L;
        Page<ItemRequest> requests = requestRepository
                .findAllByRequestorIdNot(user2Id, PageRequest.of(0, 1));

        Assertions.assertEquals(1, requests.getTotalPages());
        Assertions.assertEquals(1L, requests.getTotalElements());
    }

    @Test
    void findAllByRequestorIdOrderByCreatedAsc() {
        entityManager.persist(request);
        entityManager.flush();
        entityManager.persist(user);

        List<ItemRequest> requests = requestRepository.findByRequestorOrderByCreated(user);

        Assertions.assertEquals(1, requests.size());
        Assertions.assertEquals(1L, requests.get(0).getId());
        Assertions.assertEquals(request.getDescription(), requests.get(0).getDescription());
        Assertions.assertEquals(request.getCreated(), requests.get(0).getCreated());
        Assertions.assertEquals(user, requests.get(0).getRequestor());
    }

}
