package ru.practicum.shareit.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class IncorrectUserParameterException extends RuntimeException {
    private final String parameter;
}
