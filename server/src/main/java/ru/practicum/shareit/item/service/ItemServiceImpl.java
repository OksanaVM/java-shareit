package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.ItemBookingInfoDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.IncorrectEntityParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.OutputItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
        if (ownerId == null) {
            throw new IncorrectEntityParameterException("Owner ID не может быть null");
        }
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new IncorrectEntityParameterException("Название не может быть пустой");
        } else if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new IncorrectEntityParameterException("Описание не может быть пустой");
        } else if (itemDto.getAvailable() == null) {
            throw new IncorrectEntityParameterException("Статус не может быть пустой");
        } else {
            checkOwner(ownerId);
            Item item = ItemMapper.toItem(itemDto);
            if (itemDto.getRequestId() != null) {
                item.setRequestId(itemDto.getRequestId());
            }
            Optional<User> user = userRepository.findById(ownerId);
            item.setOwner(user.get());
            Item newItem = itemRepository.save(item);

            return ItemMapper.toItemDto(newItem);
        }
    }

    @Transactional
    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {

        Item oldItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Информация по товару не найдена"));

        User user = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        item.setId(itemId);
        item.setIsAvailable(item.getIsAvailable() != null ? item.getIsAvailable() : oldItem.getIsAvailable());
        item.setName(item.getName() != null && !item.getName().isBlank() ? item.getName() : oldItem.getName());
        item.setDescription(item.getDescription() != null && !item.getDescription().isBlank() ? item.getDescription() : oldItem.getDescription());
        item.setRequestId(item.getRequestId() != null ? item.getRequestId() : oldItem.getRequestId());

        Item newItem = itemRepository.save(item);
        return ItemMapper.toItemDto(newItem);
    }

    @Override
    public List<OutputItemDto> getItems(Long ownerId, int from, int size) {
        Pageable page = PageRequest.of(from / size, size);
        checkOwner(ownerId);
        return fillWithBookingInfo(itemRepository.findByOwnerIdOrderById(ownerId, page), ownerId);
    }

    @Override
    public List<ItemDto> getItems(String text, int from, int size) {
        Pageable page = PageRequest.of(from / size, size);
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return ItemMapper.toItemDtoList(itemRepository.searchItems(text, page));
    }

    @Override
    @Transactional
    public CommentDto addComment(Long authorId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(authorId).orElseThrow(() ->
                new NotFoundException("Автор не найден"));
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Предмет не найден"));
        List<Booking> authorBooked = bookingRepository.findBookingsByItem(item, BookingStatus.APPROVED, authorId, LocalDateTime.now());
        if (authorBooked.isEmpty()) {
            throw new IncorrectEntityParameterException("Неверные параметры");
        }
        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item);
        comment.setText(commentDto.getText());
        Comment result = commentRepository.save(comment);
        CommentDto newComment = CommentMapper.toCommentDto(result);
        newComment.setAuthorName(user.getName());
        return newComment;

    }

    @Override
    public OutputItemDto getItem(Long itemId, Long userId) {
        Item newItem = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Предмет не найден"));
        return fillWithBookingInfo(List.of(newItem), userId).get(0);
    }

    private List<OutputItemDto> fillWithBookingInfo(List<Item> items, Long userId) {
        Map<Item, List<Comment>> comments = commentRepository.findByItemIn(items, Sort.by(DESC, "created"))
                .stream()
                .collect(groupingBy(Comment::getItem, toList()));
        Map<Item, List<Booking>> bookings = bookingRepository.findByItemInAndStatus(items, BookingStatus.APPROVED, Sort.by(DESC, "start"))
                .stream()
                .collect(groupingBy(Booking::getItem, toList()));
        LocalDateTime now = LocalDateTime.now();
        return items.stream().map(item -> addBookingAndComment(item, userId, comments.getOrDefault(item, List.of()),
                        bookings.getOrDefault(item, List.of()), now))
                .collect(toList());
    }

    public OutputItemDto addBookingAndComment(Item item, Long userId, List<Comment> comments, List<Booking> bookings, LocalDateTime now) {
        if (!item.getOwner().getId().equals(userId)) {
            return ItemMapper.toItemsDto(item, null, null, CommentMapper.commentDtoList(comments));
        }

        Booking lastBooking = bookings.stream()
                .filter(b -> !b.getStart().isAfter(now))
                .findFirst()
                .orElse(null);

        Booking nextBooking = bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .reduce((a, b) -> a.getStart().isBefore(b.getStart()) ? a : b)
                .orElse(null);

        ItemBookingInfoDto lastBookingDto = lastBooking != null
                ? BookingMapper.toItemBookingInfoDto(lastBooking) : null;
        ItemBookingInfoDto nextBookingDto = nextBooking != null
                ? BookingMapper.toItemBookingInfoDto(nextBooking) : null;
        return ItemMapper.toItemsDto(item, lastBookingDto, nextBookingDto, CommentMapper.commentDtoList(comments));
    }

    private void checkOwner(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

}
