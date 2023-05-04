package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.ItemBookingInfoDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.IncorrectEntityParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemsDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;


@Service
@RequiredArgsConstructor
@Slf4j
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
            Optional<User> user = userRepository.findById(ownerId);
            item.setOwner(user.get());
            Item newItem = itemRepository.save(item);

            return ItemMapper.toItemDto(newItem);
        }
    }

    @Transactional
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
            if (item.getName() == null || item.getName().isBlank()) {
                item.setName(oldItem.getName());
            }
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                item.setDescription(oldItem.getDescription());
            }
            if (item.getRequestId() == null) {
                item.setRequestId(oldItem.getRequestId());
            }
            Item newItem = itemRepository.save(item);
            return ItemMapper.toItemDto(newItem);
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    @Transactional
    @Override
    public ItemsDto getItem(Long itemId, Long userId) {
        Item newItem = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Предмет не найден"));
        return fillWithBookingInfo(List.of(newItem), userId).get(0);
    }

    @Transactional
    @Override
    public List<ItemsDto> getItems(Long ownerId) {
        checkOwner(ownerId);
        return fillWithBookingInfo(itemRepository.findAllByOwnerIdOrderById(ownerId), ownerId);
    }

    @Transactional
    @Override
    public List<ItemDto> getItems(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return ItemMapper.toItemDtoList(itemRepository.findAllItemsByLike(text));
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
        commentRepository.save(comment);
        CommentDto newComment = CommentMapper.toCommentDto(comment);
        newComment.setAuthorName(user.getName());
        return newComment;

    }

    private List<ItemsDto> fillWithBookingInfo(List<Item> items, Long userId) {
        //получили все комменты и букинги
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

    private ItemsDto addBookingAndComment(Item item, Long userId, List<Comment> comments, List<Booking> bookings, LocalDateTime now) {
        if (!item.getOwner().getId().equals(userId)) {
            return ItemMapper.toItemsDto(item, null, null, CommentMapper.commentDtoList(comments));
        }

        Booking lastBooking = bookings.stream()
                .filter(b -> !b.getStart().isAfter(now))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
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
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

}
