package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.user.model.User;

import java.util.Date;

@Data
public class ItemRequestDto {
    private String description;
    private User requestor;
    private Date created;
}
