package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {

    List<User> getUsers();

    User getUserByEmail(String email);

    User addUser(User user);

    void deleteUser(Long id);

    void updateUser(User user);

    User getUserById(Long id);
}
