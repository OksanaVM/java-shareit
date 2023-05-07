package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.Create;
import ru.practicum.shareit.user.dto.Update;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
//ksjach/askjjhb.kjh;sdjk
    @GetMapping()
    public List<UserDto> getUsers() {
        return userService.getUsersList();
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PostMapping()
    public UserDto create(@Validated(Create.class) @RequestBody UserDto user) {
        return userService.addUser(user);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @Validated(Update.class) @RequestBody UserDto user) {
        return userService.updateUser(new UserDto(id, user.getName(), user.getEmail()));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
