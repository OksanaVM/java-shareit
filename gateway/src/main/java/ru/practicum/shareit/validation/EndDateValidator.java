package ru.practicum.shareit.validation;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class EndDateValidator implements ConstraintValidator<ValidEndDate, BookItemRequestDto> {
    @Override
    public boolean isValid(BookItemRequestDto value, ConstraintValidatorContext context) {
        return value.getStart().isBefore(value.getEnd());
    }
}