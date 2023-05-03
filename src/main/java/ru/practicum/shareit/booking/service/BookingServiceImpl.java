package ru.practicum.shareit.booking.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
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
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.booking.mapper.BookingMapper.toBookingDto;

@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final UserService userService;

    @Transactional
    @Override
    public BookingDto addBooking(Long bookerId, BookingDtoShort bookingDto) {
        checkDates(bookingDto);

        Optional<Item> item = itemRepository.findById(bookingDto.getItemId());
        if (item.isPresent() && item.get().getIsAvailable()) {
            Long ownerId = item.get().getOwner().getId();
            if (ownerId.equals(bookerId)) {
                throw new NotFoundException("Неверные параметры");
            }
            Booking booking = new Booking();
            Optional<User> user = userRepository.findById(bookerId);
            if (user.isPresent()) {
                booking.setBooker(user.get());
            } else {
                throw new NotFoundException("Пользователь не найден");
            }
            booking.setStart(bookingDto.getStart());
            booking.setEnd(bookingDto.getEnd());
            booking.setItem(item.get());
            booking.setStatus(BookingStatus.WAITING);

            bookingRepository.save(booking);

            return toBookingDto(booking);

        } else if (item.isEmpty()) {
            throw new NotFoundException("Вещи с таким id нет");
        } else {
            throw new IncorrectEntityParameterException("Вещь недоступна");
        }
    }

    private void checkDates(BookingDtoShort bookingDto) {
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new TimeDataException("Ошибка со временем бронирования");
        }
    }

    @Transactional
    @Override
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Optional<Booking> bookingOption = bookingRepository.findById(bookingId);
        if (bookingOption.isPresent()) {
            Item item = bookingOption.get().getItem();
            User owner = item.getOwner();
            if (owner.getId().equals(ownerId)) {
                Booking booking = bookingOption.get();
                if (!BookingStatus.WAITING.equals(booking.getStatus())) {
                    throw new RequestFailedException("Статус уже проставлен");
                }
                BookingStatus status;
                if (approved) {
                    status = BookingStatus.APPROVED;
                } else {
                    status = BookingStatus.REJECTED;
                }
                booking.setStatus(BookingStatus.valueOf(String.valueOf(status)));

                bookingRepository.save(booking);
                return BookingMapper.toBookingDto(booking);
            } else {
                throw new NotFoundException("Неверные параметры");
            }
        } else {
            throw new NotFoundException("Брони с такой ID нет");
        }
    }

    @Transactional
    @Override
    public BookingDto getBooking(Long bookerId, Long id) {
        Optional<Booking> bookingOption = bookingRepository.findById(id);
        if (bookingOption.isPresent()) {
            Booking booking = bookingOption.get();
            if (booking.getBooker().getId().equals(bookerId) || booking.getItem().getOwner().getId().equals(bookerId)) {
                return BookingMapper.toBookingDto(booking);
            } else {
                throw new NotFoundException("Неверные параметры");
            }
        } else {
            throw new NotFoundException("Брони с таким id нет");
        }
    }

    @Transactional
    @Override
    public List<BookingDto> getBooking(String state, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("Пользователь не найден");
        });
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case "ALL":
                return BookingMapper.toBookingDtoList(bookingRepository.findAllByBooker_IdOrderByStartDesc(userId));
            case "CURRENT":
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndEndIsAfterAndStartIsBeforeOrderByStartDesc(userId, now, now));
            case "PAST":
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(userId, now));
            case "FUTURE":
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndStartIsAfterOrderByStartDesc(userId, now));
            case "WAITING":
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndStartIsAfterAndStatusIsOrderByStartDesc(userId, now,
                                BookingStatus.WAITING));
            case "REJECTED":
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findAllByBooker_IdAndStatusIsOrderByStartDesc(userId, BookingStatus.REJECTED));

        }
        throw new RequestFailedException(String.format("Unknown state: %s", state));
    }

    @Transactional
    @Override
    public List<BookingDto> ownerItemsBookingLists(String state, Long ownerId) {
        userRepository.findById(ownerId).orElseThrow(() -> {
            throw new NotFoundException("Пользователь не найден");
        });
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case "ALL":
                return BookingMapper.toBookingDtoList(bookingRepository.findAllByOwnerItems(ownerId));
            case "CURRENT":
                return BookingMapper.toBookingDtoList(bookingRepository.findAllCurrentBookingsOwner(ownerId, now));
            case "PAST":
                return BookingMapper.toBookingDtoList(bookingRepository.findAllPastBookingsOwner(ownerId, now));
            case "FUTURE":
                return BookingMapper.toBookingDtoList(bookingRepository.findAllFutureBookingsOwner(ownerId, now));
            case "WAITING":
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findByOwnerAndState(ownerId, BookingStatus.WAITING));
            case "REJECTED":
                return BookingMapper.toBookingDtoList(bookingRepository
                        .findByOwnerAndState(ownerId, BookingStatus.REJECTED));
        }
        throw new RequestFailedException(String.format("Unknown state: %s", state));
    }


}
