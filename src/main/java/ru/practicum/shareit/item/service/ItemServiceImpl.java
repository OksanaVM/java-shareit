package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
        return fillWithBookingInfo(newItem, userId);
    }

    @Transactional
    @Override
    public List<ItemsDto> getItems(Long ownerId) {
        checkOwner(ownerId);
        return itemRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(x -> fillWithBookingInfo(x, ownerId))
                .sorted(Comparator.comparing(ItemsDto::getId))
                .collect(Collectors.toList());
    }

    private ItemsDto fillWithBookingInfo(Item item, Long userId) {
        if (!item.getOwner().getId().equals(userId)) {
            return ItemMapper.toItemsDto(item, null, null, getComment(item));
        }
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findAllBookingsItem(item.getId());
        Booking lastBooking = bookings.stream()
                .filter(b -> b.getStart().isBefore(now))
                .max(Comparator.comparing(Booking::getStart))
                .orElse(null);
        Booking nextBooking = bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        ItemBookingInfoDto lastBookingDto = lastBooking != null
                ? BookingMapper.toItemBookingInfoDto(lastBooking) : null;
        ItemBookingInfoDto nextBookingDto = nextBooking != null
                ? BookingMapper.toItemBookingInfoDto(nextBooking) : null;
        return ItemMapper.toItemsDto(item, lastBookingDto, nextBookingDto, getComment(item));


    }

    @Transactional
    @Override
    public List<ItemDto> getItems(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return ItemMapper.toItemDtoList(itemRepository.findAllItemsByLike(text));
    }

    private List<CommentDto> getComment(Item item) {
        List<Comment> itemCommentList = commentRepository.findByItem(item);
        return CommentMapper.commentDtoList(itemCommentList);

    }

    private void checkOwner(Long ownerId) {
        Optional<User> user = userRepository.findById(ownerId);
        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    @Override
    @Transactional
    public CommentDto addComment(Long authorId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(authorId).orElseThrow(() -> new NotFoundException("Автор не найден"));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Предмет не найден"));

        List<Booking> authorBooked = bookingRepository.findByItemAndBooker(item, author)
                .stream()
                .filter(booking -> booking.getStatus().equals(BookingStatus.APPROVED))
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                .sorted(Comparator.comparing(Booking::getStart).reversed()).collect(Collectors.toList());

        if (authorBooked.isEmpty()) {
            throw new IncorrectEntityParameterException("Неверные параметры");
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
