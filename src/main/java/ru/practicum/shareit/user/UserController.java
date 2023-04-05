package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final String pathId = "/{id}";

    @GetMapping()
    public List<UserDto> getUsers() {
        return userService.getUsersList();
    }

    @GetMapping(pathId)
    public UserDto getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PostMapping()
    public UserDto create(@Valid @RequestBody UserDto user) {
        return userService.addUser(user);
    }

    @PatchMapping(pathId)
    public UserDto update(@PathVariable Long id, @RequestBody @NotNull UserDto user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping(pathId)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
