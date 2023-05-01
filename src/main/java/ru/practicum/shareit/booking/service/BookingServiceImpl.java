package ru.practicum.shareit.booking.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
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

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public BookingDto addBooking(Long bookerId, BookingDto bookingDto) {
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

    private void checkDates(BookingDto bookingDto) {
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
    public List<BookingDto> getBooking(String state, Long bookerId, Integer from, Integer size) {
        Optional<User> user = userRepository.findById(bookerId);
        if (user.isPresent()) {
            List<Booking> bookingList;
            if (from == null && size == null) {
                bookingList = bookingRepository.findByBooker(user.get());
            } else if (from >= 0 && size > 0) {
                bookingList = bookingRepository.findByBookerByPage(Math.toIntExact(user.get().getId()), from, size);
            } else {
                throw new NotFoundException("Неверные параметры");
            }

            List<Booking> list = getBookingListByStatus(state, bookingList);
            return BookingMapper.toBookingDtoList(list);
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private List<Booking> getBookingListByStatus(String state, List<Booking> bookingList) {
        switch (state) {
            case "PAST":
                return bookingList.stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                        .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                        .collect(Collectors.toList());

            case "FUTURE":
                return bookingList.stream()
                        .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()) && booking.getEnd().isAfter(LocalDateTime.now()))
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                        .collect(Collectors.toList());

            case "CURRENT":
                return bookingList.stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                        .filter(booking -> booking.getEnd().isAfter(LocalDateTime.now()))
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                        .collect(Collectors.toList());

            case "WAITING":
                return bookingList.stream()
                        .filter(booking -> BookingStatus.WAITING.equals(booking.getStatus()))
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                        .collect(Collectors.toList());

            case "REJECTED":
                return bookingList.stream()
                        .filter(booking -> BookingStatus.REJECTED.equals(booking.getStatus()))
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                        .collect(Collectors.toList());

            case "ALL":
                return bookingList.stream()
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                        .collect(Collectors.toList());

        }
        throw new RequestFailedException(String.format("Unknown state: %s", state));
    }

    @Transactional
    @Override
    public List<BookingDto> ownerItemsBookingLists(String state, Long ownerId, Integer from, Integer size) {
        Optional<User> user = userRepository.findById(ownerId);
        if (user.isPresent()) {
            List<Item> ownerItemList = itemRepository.findByOwner(user.get());
            List<Booking> bookingList = new ArrayList<>();
            ownerItemList.forEach(item -> {
                        List<Booking> itemBookingList;

                        if (from == null && size == null) {
                            itemBookingList = bookingRepository.findByItem(item);
                        } else if (from >= 0 && size > 0) {
                            itemBookingList = bookingRepository.findByItemByLimits(item.getId(), from, size);
                        } else {
                            throw new NotFoundException("Неверные параметры");
                        }
                        bookingList.addAll(itemBookingList);
                    }
            );
            List<Booking> list = getBookingListByStatus(state, bookingList);
            return BookingMapper.toBookingDtoList(list);
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }
}
