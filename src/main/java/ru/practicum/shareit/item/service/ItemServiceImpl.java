package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;

import ru.practicum.shareit.booking.repository.BookingRepository;
;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.IncorrectItemParameterException;
import ru.practicum.shareit.item.exception.IncorrectOwnerParameterException;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    //    private final ItemStorage itemStorage;
//    private final UserStorage userStorage;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
//        log.debug(String.valueOf(itemDto.getAvailable()));
        if (ownerId == null) {
            throw new IncorrectItemParameterException("Owner ID не может быть null");
        }

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new IncorrectItemParameterException("Название не может быть пустой");
        } else if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new IncorrectItemParameterException("Описание не может быть пустой");
        } else if (itemDto.getAvailable() == null) {
            throw new IncorrectItemParameterException("Статус не может быть пустой");
        } else {
            checkOwner(ownerId);
            Item item = ItemMapper.toItem(itemDto);
            Optional<User> user = userRepository.findById(ownerId);
            item.setOwner(user.get());
            Item newItem = itemRepository.save(item);

            return ItemMapper.toItemDto(newItem);
        }
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        checkOwner(ownerId);

        Item oldItem = itemRepository.findById(itemId).get();
        if (oldItem.getOwner().getId().equals(ownerId)) {
            Item item = ItemMapper.toItem(itemDto);
            User user = userRepository.findById(ownerId).get();
            item.setOwner(user);
            item.setId(itemId);
            if (item.getIsAvailable() == null) {
                item.setIsAvailable(oldItem.getIsAvailable());
            }
            if (item.getName() == null) {
                item.setName(oldItem.getName());
            }
            if (item.getDescription() == null) {
                item.setDescription(oldItem.getDescription());
            }
            if (item.getRequestId() == null) {
                item.setRequestId(oldItem.getRequestId());
            }
            Item newItem = itemRepository.save(item);
            return ItemMapper.toItemDto(newItem);
        } else {
            throw new IncorrectOwnerParameterException("Пользователь не найден");
        }
    }

    @Override
    public ItemDto getItem(Long itemId) {
        Item newItem = itemRepository.findById(itemId).orElseThrow(()-> new NotFoundException("Предмет не найден"));
        return ItemMapper.toItemDto(newItem);
    }

    @Override
    public List<ItemDto> getItems(Long ownerId) {
        checkOwner(ownerId);
        return ItemMapper.toItemDtoList(itemRepository.findAllByOwnerId(ownerId));
    }

    @Override
    public List<ItemDto> getItems(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return ItemMapper.toItemDtoList(itemRepository.findAllItemsByLike(text));
    }

    private List<CommentDto> getComment(Item item) {
        List<CommentDto> list = new ArrayList<>();
        List<Comment> itemCommentList = commentRepository.findByItem(item);
        if (itemCommentList.size() > 0) {
            list = CommentMapper.commentDtoList(itemCommentList);
        }

        return list;
    }

    private void checkOwner(Long ownerId) {
        Optional<User> user = userRepository.findById(ownerId);
        if (!user.isPresent()) {
            throw new IncorrectOwnerParameterException("Пользователь не найден");
        }
    }

    @Override
    @Transactional
    public CommentDto addComment(Long authorId, Long itemId, CommentDto commentDto) {
        Optional<User> authorOption = userRepository.findById(authorId);
        if (!authorOption.isPresent()) {
            throw new UserNotFoundException("Автор не найден");
        }
        User author = authorOption.get();

        Optional<Item> itemOption = itemRepository.findById(itemId);
        Item item = itemOption.get();

        List<Booking> authorBooked = bookingRepository.findByItemAndBooker(item, author).stream()
                .filter(booking -> booking.getStatus().equals("APPROVED"))
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());

        if (authorBooked.isEmpty()) {
            throw new IncorrectItemParameterException("Неверные параметры");
        }

        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item);
        comment.setText(commentDto.getText());
        commentRepository.save(comment);

        CommentDto newComment = CommentMapper.toCommentDto(comment);
        newComment.setAuthorName(author.getName());
        return newComment;

    }
}
