package ru.practicum.shareit.item.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class IncorrectItemRequestException extends RuntimeException {
    private final String parameter;
}
