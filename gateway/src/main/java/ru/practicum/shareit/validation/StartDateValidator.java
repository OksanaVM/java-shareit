package ru.practicum.shareit.validation;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class StartDateValidator implements ConstraintValidator<ValidStartDate, BookItemRequestDto> {
    @Override
    public boolean isValid(BookItemRequestDto value, ConstraintValidatorContext context) {
        return !value.getStart().isEqual(value.getEnd());
    }
}