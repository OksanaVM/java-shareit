package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping()
    public List<UserDto> getUsers() {
        return userService.getUsersList();
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PostMapping()
    public UserDto create(@RequestBody UserDto user) {
        System.out.println(user);
        return userService.addUser(user);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
