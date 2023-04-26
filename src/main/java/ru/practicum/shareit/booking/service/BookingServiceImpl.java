package ru.practicum.shareit.booking.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;

import ru.practicum.shareit.booking.exception.TimeDataException;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.*;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.toBookingDto;

@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto addBooking(Long bookerId, BookingDto bookingDto) {
        checkDates(bookingDto);

        Optional<Item> item = itemRepository.findById(bookingDto.getItemId());
        if (item.isPresent() && item.get().getIsAvailable()) {
            Long ownerId = item.get().getOwner().getId();
            if (ownerId.equals(bookerId)) {
                throw new IncorrectParameterException("Неверные параметры");
            }

            Booking booking = new Booking();
            Optional<User> user = userRepository.findById(bookerId);
            if (user.isPresent()) {
                booking.setBooker(user.get());
            } else {
                throw new UserNotFoundException("Пользователь не найден");
            }
            booking.setStart(bookingDto.getStart());
            booking.setEnd(bookingDto.getEnd());
            booking.setItem(item.get());
            booking.setStatus(BookingStatus.valueOf("WAITING"));

            bookingRepository.save(booking);

            return toBookingDto(booking);

        } else if (!item.isPresent()) {
            throw new IncorrectParameterException("Вещи с таким id нет");
        } else {
            throw new IncorrectBookingParameterException("Вещь недоступна");
        }
    }

        private void checkDates(BookingDto bookingDto) {
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new TimeDataException ("Ошибка со временем бронирования");
        }
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Optional<Booking> bookingOption = bookingRepository.findById(bookingId);
        if (bookingOption.isPresent()) {
            Item item = bookingOption.get().getItem();
            User owner = item.getOwner();
            if (owner.getId().equals(ownerId)) {
                Booking booking = bookingOption.get();
                if (booking.getStatus().equals("APPROVED")) {
                    throw new IncorrectBookingParameterException("Неверные параметры");
                }
                String status;
                if (approved) {
                    status = "APPROVED";
                } else {
                    status = "REJECTED";
                }
                booking.setStatus(BookingStatus.valueOf(status));

                bookingRepository.save(booking);
                return BookingMapper.toBookingDto(booking);
            } else {
                throw new MissingIdException("Неверные параметры");
            }
        } else {
            throw new MissingIdException("Брони с такой ID нет");
        }
    }

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


    @Override
    public List<BookingDto> getBooking(BookingStatus state, Long bookerId, Integer from, Integer size) {
        Optional<User> user = userRepository.findById(bookerId);
        if (user.isPresent()) {
            List<Booking> bookingList = new ArrayList<>();
            if (from == null && size == null) {
                bookingList = bookingRepository.findByBooker(user.get());
            } else if (from >= 0 && size > 0) {
                bookingList = bookingRepository.findByBookerByPage(Math.toIntExact(user.get().getId()), from, size);
            } else {
                throw new IncorrectBookingParameterException("Неверные параметры");
            }

            List<Booking> list = getBookingListByStatus(state, bookingList);
            return BookingMapper.toBookingDtoList(list);
        } else {
            throw new UserNotFoundException("Пользователь не найден");
        }
    }

    private List<Booking> getBookingListByStatus(BookingStatus state, List<Booking> bookingList) {
        List<Booking> list = new ArrayList<>();
        switch (state) {
            case PAST:
                list = bookingList.stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                        .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                        .collect(Collectors.toList());
                break;
            case FUTURE:
                list = bookingList.stream()
                        .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()) && booking.getEnd().isAfter(LocalDateTime.now()))
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                    .collect(Collectors.toList());
                break;
            case CURRENT:
                list = bookingList.stream()
                        .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()))
                        .filter(booking -> booking.getEnd().isAfter(LocalDateTime.now()))
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                        .collect(Collectors.toList());
                break;
            case WAITING:
                list = bookingList.stream()
                        .filter(booking -> booking.getStatus().equals("WAITING"))
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                        .collect(Collectors.toList());
                break;
            case REJECTED:
                list = bookingList.stream()
                        .filter(booking -> booking.getStatus().equals("REJECTED"))
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                        .collect(Collectors.toList());
                break;
            case ALL:
                list = bookingList.stream()
                        .sorted(Comparator.comparing(Booking::getStart).reversed())
                        .collect(Collectors.toList());
                break;
        }
        return list;
    }

    @Override
    public List<BookingDto> ownerItemsBookingLists(BookingStatus state, Long ownerId, Integer from, Integer size) {
        Optional<User> user = userRepository.findById(ownerId);
        if (user.isPresent()) {
            List<Item> ownerItemList = itemRepository.findByOwner(user.get());
            List<Booking> bookingList = new ArrayList<>();
            ownerItemList.forEach(item -> {
                        List<Booking> itemBookingList = new ArrayList<>();
                        if (from == null && size == null) {
                            itemBookingList = bookingRepository.findByItem(item);
                        } else if (from >= 0 && size > 0) {
                            itemBookingList = bookingRepository.findByItemByLimits(item.getId(), from, size);
                        } else {
                            throw new IncorrectBookingParameterException("Неверные параметры");
                        }
                        bookingList.addAll(itemBookingList);
                    }
            );
            List<Booking> list = getBookingListByStatus(state, bookingList);
            return BookingMapper.toBookingDtoList(list);
        } else {
            throw new UserNotFoundException("Пользователь не найден");
        }
    }
}
