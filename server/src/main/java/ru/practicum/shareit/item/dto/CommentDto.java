package ru.practicum.shareit.item.dto;

import lombok.*;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentDto {
    private Long id;

    private String text;
    private AuthorDto author;
    private String authorName;
    private LocalDateTime created;

}
