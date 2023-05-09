package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceIntegTest {
    private final EntityManager entityManager;
    private final UserService userService;

    @Test
    void createUser() {
        UserDto userDto = new UserDto(1L, "testUser", "test@email.com");
        userService.addUser(userDto);


        TypedQuery<User> query = entityManager
                .createQuery("Select u from User u where u.email = :email", User.class);
        User user = query
                .setParameter("email", userDto.getEmail())
                .getSingleResult();

        assertThat(user.getId(), equalTo(1L));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }


    @Test
    void getUserById() {
        UserDto userDto = new UserDto(null, "testUser", "test@email.com");
        userService.addUser(userDto);

        UserDto user = userService.getUser(1L);

        assertThat(user.getId(), equalTo(1L));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void editUser() {
        UserDto userToCreate = new UserDto(1L, "testUser", "test@email.com");
        UserDto userToUpdate = new UserDto(1L, "testUserUpdate", "testUserUpdate@email.com");
        UserDto userToUpdate2 = new UserDto(1L, "testUserUpdate", "testUserUpdate2@email.com");
        UserDto userToUpdate3 = new UserDto(1L, "testUserUpdate3", "testUserUpdate@email.com");

        userService.addUser(userToCreate);

        UserDto updatedUser = userService.updateUser(1L, userToUpdate);

        assertThat(updatedUser.getName(), equalTo(userToUpdate.getName()));
        assertThat(updatedUser.getEmail(), equalTo(userToUpdate.getEmail()));

        updatedUser = userService.updateUser(1L, userToUpdate2);

        assertThat(updatedUser.getEmail(), equalTo(userToUpdate2.getEmail()));

        updatedUser = userService.updateUser(1L, userToUpdate3);

        assertThat(updatedUser.getName(), equalTo(userToUpdate3.getName()));
    }

    @Test
    void deleteUser() {
        UserDto userDto = new UserDto(1L, "testUser", "test@email.com");
        userService.addUser(userDto);

        userService.deleteUser(1L);

        Exception e = Assertions.assertThrows(NotFoundException.class, () -> userService.getUser(1L));
        assertThat(e.getMessage(), equalTo("user по id 1 не найден"));
    }

}
