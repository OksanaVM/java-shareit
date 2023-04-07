package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exceptions.IncorrectUserParameterException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public UserDto addUser(UserDto userDto) {
        User userByEmail = userStorage.getUserByEmail(userDto.getEmail());
        if (userByEmail == null) {
            User user = UserMapper.toUserModel(userDto);
            User newUser = userStorage.addUser(user);
            return UserMapper.toUserDto(newUser);
        } else {
            throw new IncorrectUserParameterException("Такой email уже существует");
        }
    }

    public void deleteUser(Long userId) {
        if (userId != null) {
            userStorage.deleteUser(userId);
        }
    }


    public UserDto updateUser(Long id, UserDto userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("UserDto не может быть нулевым");
        }
        User userByEmail = userStorage.getUserByEmail(userDto.getEmail());
        if (userByEmail != null && !userByEmail.getId().equals(id)) {
            throw new IncorrectUserParameterException("Такой email уже существует");
        }
        User user = UserMapper.toUserModel(userDto);
        userStorage.updateUser(id, user);
        User newUser = userStorage.getUserById(id);
        return UserMapper.toUserDto(newUser);
    }

    public List<UserDto> getUsersList() {
        return UserMapper.toUserDtoList(userStorage.getUsers());
    }

    public UserDto getUser(Long id) {
        User user = userStorage.getUserById(id);
        if (user != null) {
            return UserMapper.toUserDto(user);
        } else {
            throw new IncorrectUserParameterException("Пользователь не найден");
        }
    }
}
