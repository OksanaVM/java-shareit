package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.toUserModel(userDto);
        User newUser = userRepository.save(user);
        return UserMapper.toUserDto(newUser);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        if (userId != null) {
            userRepository.deleteById(userId);
        }
    }

    @Transactional
    @Override
    public UserDto updateUser(UserDto userDto) {
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new NotFoundException("пользователь не найден " + userDto.getId()));

        if (userDto.getName() != null && !(userDto.getName().isBlank())) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !(userDto.getEmail().isBlank())) {
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(user);
    }

    @Transactional
    @Override
    public List<UserDto> getUsersList() {
        return UserMapper.toUserDtoList(userRepository.findAll());
    }

    @Transactional
    @Override
    public UserDto getUser(Long id) {
        return UserMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("user по id %d не найден", id))));
    }
}
