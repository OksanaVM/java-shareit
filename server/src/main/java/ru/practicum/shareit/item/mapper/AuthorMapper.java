package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.AuthorDto;
import ru.practicum.shareit.user.model.User;

public class AuthorMapper {

    public static AuthorDto toAuthor(User user) {
        return new AuthorDto(user.getId(), user.getName(), user.getEmail());
    }
}
