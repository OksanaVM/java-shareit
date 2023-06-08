package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.DefaultValidation;
import ru.practicum.shareit.validation.TimeComparing;
import ru.practicum.shareit.validation.ValidEndDate;
import ru.practicum.shareit.validation.ValidStartDate;

import javax.validation.GroupSequence;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@GroupSequence({BookItemRequestDto.class, DefaultValidation.class, TimeComparing.class})
@ValidStartDate(groups = {TimeComparing.class})
@ValidEndDate(groups = {TimeComparing.class})
public class BookItemRequestDto implements Serializable {
    @NotNull(groups = {DefaultValidation.class})
    private long itemId;
    @NotNull(groups = {DefaultValidation.class})
    @FutureOrPresent(groups = {DefaultValidation.class})
    private LocalDateTime start;
    @NotNull(groups = {DefaultValidation.class})
    @FutureOrPresent(groups = {DefaultValidation.class})
    private LocalDateTime end;
}
