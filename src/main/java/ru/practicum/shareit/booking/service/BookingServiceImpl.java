package ru.practicum.shareit.booking.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.IncorrectEntityParameterException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.RequestFailedException;
import ru.practicum.shareit.exceptions.TimeDataException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.mapper.BookingMapper.toBookingDto;

@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;


    @Transactional
    @Override
    public BookingDto addBooking(Long bookerId, BookingDtoShort bookingDto) {
        checkDates(bookingDto);

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещи с таким id нет"));

        if (!item.getIsAvailable()) {
            throw new IncorrectEntityParameterException("Вещь недоступна");
        }

        Long ownerId = item.getOwner().getId();
        if (ownerId.equals(bookerId)) {
            throw new NotFoundException("Неверные параметры");
        }

        Booking booking = new Booking();
        User user = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        booking.setBooker(user);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);
        return toBookingDto(booking);
    }

    @Transactional
    @Override
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Брони с такой ID нет"));
        Item item = booking.getItem();
        User owner = item.getOwner();
        if (!owner.getId().equals(ownerId)) {
            throw new NotFoundException("Неверные параметры");
        }
        if (!BookingStatus.WAITING.equals(booking.getStatus())) {
            throw new RequestFailedException("Статус уже проставлен");
        }
        BookingStatus status = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(status);

        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Transactional
    @Override
    public BookingDto getBooking(Long bookerId, Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Брони с таким id нет"));
        if (booking.getBooker().getId().equals(bookerId) || booking.getItem().getOwner().getId().equals(bookerId)) {
            return BookingMapper.toBookingDto(booking);
        } else {
            throw new NotFoundException("Неверные параметры");
        }
    }

    @Transactional
    @Override
    public List<BookingDto> getBooking(String state, Long userId, int from, int size) {
        userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("Пользователь не найден");
        });
        BookingState stateFromText = BookingState.getStateFromText(state);
        Pageable page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        switch (stateFromText) {
            case ALL:
                return BookingMapper.toBookingDtoList(bookingRepository.findAllByBooker_IdOrderByStartDesc(userId, page));
            case CURRENT:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(userId, now, now, page));
            case PAST:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(userId, now, page));
            case FUTURE:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndStartIsAfterOrderByStartDesc(userId, now, page));
            case WAITING:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndStartIsAfterAndStatusIsOrderByStartDesc(userId, now,
                                BookingStatus.WAITING, page));
            case REJECTED:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndStatusIsOrderByStartDesc(userId, BookingStatus.REJECTED, page));
        }
        throw new RequestFailedException(String.format("Unknown state: %s", state));
    }

    @Transactional
    @Override
    public List<BookingDto> ownerItemsBookingLists(String state, Long ownerId, int from, int size) {
        userRepository.findById(ownerId).orElseThrow(() -> {
            throw new NotFoundException("Пользователь не найден");
        });
        BookingState stateFromText = BookingState.getStateFromText(state);
        Pageable page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        switch (stateFromText) {
            case ALL:
                return BookingMapper.toBookingDtoList(bookingRepository.findAllByOwnerItems(ownerId, page));
            case CURRENT:
                return BookingMapper.toBookingDtoList(bookingRepository.findAllCurrentBookingsOwner(ownerId, now, page));
            case PAST:
                return BookingMapper.toBookingDtoList(bookingRepository.findAllPastBookingsOwner(ownerId, now, page));
            case FUTURE:
                return BookingMapper.toBookingDtoList(bookingRepository.findAllFutureBookingsOwner(ownerId, now, page));
            case WAITING:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findByOwnerAndState(ownerId, BookingStatus.WAITING, page));
            case REJECTED:
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findByOwnerAndState(ownerId, BookingStatus.REJECTED, page));
        }
        throw new RequestFailedException(String.format("Unknown state: %s", state));
    }

    private void checkDates(BookingDtoShort bookingDto) {
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new TimeDataException("Ошибка со временем бронирования");
        }
    }
}
