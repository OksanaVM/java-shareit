package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
public interface UserService {
    UserDto addUser(UserDto user);

    void deleteUser(Long userId);

    UserDto updateUser(long userId, UserDto user);

    List<UserDto> getUsersList();

    UserDto getUser(Long id);
}
