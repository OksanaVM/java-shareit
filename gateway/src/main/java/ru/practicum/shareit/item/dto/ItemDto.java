package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank(groups = {Create.class})
    @Size(groups = {Create.class, Update.class}, min = 1)
    private String name;
    @NotBlank(groups = {Create.class})
    @Size(groups = {Create.class, Update.class}, min = 1)
    private String description;
    @NotNull(groups = {Create.class})
    private Boolean available;
    private Long requestId;
}