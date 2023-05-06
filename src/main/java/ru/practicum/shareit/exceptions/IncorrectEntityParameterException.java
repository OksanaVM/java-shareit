package ru.practicum.shareit.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class IncorrectEntityParameterException extends RuntimeException {
    private final String parameter;
}