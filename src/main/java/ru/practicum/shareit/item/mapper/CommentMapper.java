package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(comment.getId(),
                comment.getText(),
                comment.getItem(),
                AuthorMapper.toAuthor(comment.getAuthor()),
                comment.getAuthor().getName(),
                comment.getCreated());
    }

    public static Comment toComment(CommentDto commentDto) {
        return new Comment(null,
                commentDto.getText(),
                commentDto.getItem(),
                AuthorMapper.toUser(commentDto.getAuthor()),
                commentDto.getCreated());
    }

    public static List<CommentDto> commentDtoList(List<Comment> commentList) {
        return commentList.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
