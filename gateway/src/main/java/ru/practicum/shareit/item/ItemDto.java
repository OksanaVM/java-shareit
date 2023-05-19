package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank(message = "поле 'name' не может быть пустым")
    private String name;
    @NotBlank(message = "поле 'description' не может быть пустым")
    private String description;
    @NotNull
    private Boolean available;
    private Long requestId;
}