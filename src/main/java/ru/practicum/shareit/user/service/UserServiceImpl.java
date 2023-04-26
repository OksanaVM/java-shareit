package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.IncorrectUserParameterException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    //    private final UserStorage userStorage;
    private final UserRepository userRepository;

    public UserDto addUser(UserDto userDto) {
        try {
            User user = UserMapper.toUserModel(userDto);
            User newUser = userRepository.save(user);
            return UserMapper.toUserDto(newUser);
        } catch ( Exception e) {
            throw e;
        }

    }

    public void deleteUser(Long userId) {
        if (userId != null) {
            userRepository.deleteById(userId);
        }
    }

    public UserDto updateUser(UserDto userDto) {
        User user = userRepository.findById(userDto.getId())
                        .orElseThrow(() -> new NotFoundException("пользователь не найден "+userDto.getId()));
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    public List<UserDto> getUsersList() {
        return UserMapper.toUserDtoList(userRepository.findAll());
    }

    public UserDto getUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return UserMapper.toUserDto(user.get());
        } else {
            throw new UserNotFoundException("Пользователь не найден");
        }
    }
}
