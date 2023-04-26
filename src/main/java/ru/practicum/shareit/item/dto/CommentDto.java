package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;


import lombok.ToString;
import ru.practicum.shareit.item.model.Item;

;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentDto {
    private Long id;

    @NotBlank
    private String text;

    private Item item;
    private AuthorDto author;
    private String authorName;
    private LocalDateTime created;
}
