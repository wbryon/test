package ru.practicum.shareit.booking.service;


import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.WrongRequestException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    public BookingServiceImpl(UserRepository userRepository, ItemRepository itemRepository,
                              BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public BookingDto create(Long userId, BookingDto bookingDto) {
        User booker = userRepository.findByUserId(userId);
        Item item = itemRepository.findByItemId(bookingDto.getItemId());
        if (booker.getId().equals(item.getOwner().getId()))
            throw new NotFoundException("Владелец не может арендовать собственную вещь");
        if (!item.getAvailable())
            throw new WrongRequestException("недоступно для бронирования");
        bookingDto.setStatus(Status.WAITING);
        Booking booking = BookingMapper.toBooking(bookingDto, booker, item);
        bookingDateValidation(booking);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto update(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findByBookingId(bookingId);
        User owner = userRepository.findByUserId(userId);
        if (approved == null) throw new WrongRequestException("Статус бронирования не определён");
        bookingDateValidation(booking);
        if (owner.getId().equals(booking.getItem().getOwner().getId())) {
            if (booking.getStatus().equals(Status.APPROVED))
                throw new WrongRequestException("Бронирование было подтерждено ранее");
            if (approved)
                booking.setStatus(Status.APPROVED);
            else
                booking.setStatus(Status.REJECTED);
            return BookingMapper.toBookingDto(bookingRepository.save(booking));
        } else
            throw new NotFoundException("Пользователь не является владельцем вещи");
    }

    @Override
    public BookingDto findBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId);
        User user = userRepository.findByUserId(userId);
        if (!booking.getItem().getOwner().getId().equals(user.getId()) && !booking.getBooker().getId().equals(user.getId()))
            throw new NotFoundException("Пользователь не является владельцем или арендатором");
        else
            return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookingsByOwner(Long userId, String state, Integer from, Integer size) {
        userRepository.findByUserId(userId);
        LocalDateTime time = LocalDateTime.now();
        List<Booking> bookingList;
        PageRequest request = PageRequest.of(from, size);
        switch (state) {
            case "ALL":
                bookingList = bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(userId, request);
                break;
            case "CURRENT":
                bookingList = bookingRepository
                        .findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId, time, time, request);
                break;
            case "PAST":
                bookingList = bookingRepository.findAllByItem_OwnerIdAndEndIsBeforeOrderByStartDesc(userId, time, request);
                break;
            case "FUTURE":
                bookingList = bookingRepository.findAllByItem_OwnerIdAndStartIsAfterOrderByStartDesc(userId, time, request);
                break;
            case "WAITING":
                bookingList = bookingRepository
                        .findAllByItem_OwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING, request);
                break;
            case "REJECTED":
                bookingList = bookingRepository
                        .findAllByItem_OwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, request);
                break;
            default:
                throw new WrongRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
        return BookingMapper.toBookingDtoList(bookingList);
    }

    @Override
    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public List<BookingDto> getAllBookingsByBooker(Long userId, String state, Integer from, Integer size) {
        userRepository.findByUserId(userId);
        LocalDateTime time = LocalDateTime.now();
        List<Booking> bookingList;
        PageRequest request = PageRequest.of(from / size, size);
        switch (state) {
            case "ALL":
                bookingList = bookingRepository.findAllByBookerIdOrderByStartDesc(userId, request);
                break;
            case "CURRENT":
                bookingList = bookingRepository
                        .findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStart(userId, time, time, request);
                break;
            case "PAST":
                bookingList = bookingRepository
                        .findAllByBookerIdAndEndIsBeforeOrderByStartDesc(userId, time, request);
                break;
            case "FUTURE":
                bookingList = bookingRepository
                        .findAllByBookerIdAndStartIsAfterOrderByStartDesc(userId, time, request);
                break;
            case "WAITING":
                bookingList = bookingRepository
                        .findAllByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING, request);
                break;
            case "REJECTED":
                bookingList = bookingRepository
                        .findAllByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, request);
                break;
            default:
                throw new WrongRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
        return BookingMapper.toBookingDtoList(bookingList);
    }

//    @Override
//    @Transactional
//    public BookingDto create(Long userId, BookingDto bookingDto) {
//        User booker = userRepository.findByUserId(userId);
//        Item item = itemRepository.findByItemId(bookingDto.getItemId());
//        if (booker.getId().equals(item.getOwner().getId())) {
//            throw new NotFoundException("Booker is equals owner");
//        }
//        if (!item.getAvailable()) {
//            throw new WrongRequestException("Item is not available for booking");
//        }
//        bookingDto.setStatus(Status.WAITING);
//        Booking booking = BookingMapper.toBooking(bookingDto, booker, item);
//        bookingDateValidation(booking);
//        return BookingMapper.toBookingDto(bookingRepository.save(booking));
//    }
////
//    @Override
//    @Transactional
//    public BookingDto update(Long userId, Long bookingId, Boolean approved) {
//        Booking booking = bookingRepository.findByBookingId(bookingId);
//        User owner = userRepository.findByUserId(userId);
//        if (approved == null) {
//            throw new WrongRequestException("Status is null");
//        }
//        bookingDateValidation(booking);
//        if (!owner.getId().equals(booking.getItem().getOwner().getId())) {
//            throw new NotFoundException("User is not this booking owner");
//        }
//        if (booking.getStatus().equals(Status.APPROVED)) {
//            throw new WrongRequestException("Booking is already approved");
//        }
//        if (approved) {
//            booking.setStatus(Status.APPROVED);
//        } else {
//            booking.setStatus(Status.REJECTED);
//        }
//        return BookingMapper.toBookingDto(bookingRepository.save(booking));
//    }

//    @Override
//    public BookingDto findBookingById(Long userId, Long bookingId) {
//        Booking booking = bookingRepository.findByBookingId(bookingId);
//        User user = userRepository.findByUserId(userId);
//        if (booking.getItem().getOwner().getId().equals(user.getId()) || booking.getBooker().getId().equals(user.getId())) {
//            return BookingMapper.toBookingDto(booking);
//        } else {
//            throw new NotFoundException("User is not owner or booker");
//        }
//    }
//
//    @Override
//    public List<BookingDto> getAllBookingsByOwner(Long userId, String state, Integer from, Integer size) {
//        userRepository.findByUserId(userId);
//        LocalDateTime currentTime = LocalDateTime.now();
//        List<Booking> result = null;
//        PageRequest request = PageRequest.of(from, size);
//        switch (state) {
//            case "ALL":
//                result = bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(userId, request);
//                break;
//            case "PAST":
//                result = bookingRepository.findAllByItem_OwnerIdAndEndIsBeforeOrderByStartDesc(userId, currentTime, request);
//                break;
//            case "CURRENT":
//                result = bookingRepository.findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId, currentTime, currentTime, request);
//                break;
//            case "FUTURE":
//                result = bookingRepository.findAllByItem_OwnerIdAndStartIsAfterOrderByStartDesc(userId, currentTime, request);
//                break;
//            case "WAITING":
//                result = bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING, request);
//                break;
//            case "REJECTED":
//                result = bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, request);
//                break;
//            default:
//                throw new WrongRequestException("Unknown state: UNSUPPORTED_STATUS");
//        }
//        return BookingMapper.bookingListToBookingReturnDtoList(result);
//    }
//
//    @Override
//    @Transactional
//    public void delete(Long id) {
//        bookingRepository.deleteById(id);
//    }
//
//    @Override
//    public List<BookingDto> getAllBookingsByBooker(Long userId, String state, Integer from, Integer size) {
//        userRepository.findByUserId(userId);
//        LocalDateTime currentTime = LocalDateTime.now();
//        List<Booking> result = null;
//        PageRequest request = PageRequest.of(from / size, size);
//        switch (state) {
//            case "ALL":
//                result = bookingRepository.findAllByBookerIdOrderByStartDesc(userId, request);
//                break;
//            case "PAST":
//                result = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(userId, currentTime, request);
//                break;
//            case "CURRENT":
//                result = bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStart(userId, currentTime, currentTime, request);
//                break;
//            case "FUTURE":
//                result = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(userId, currentTime, request);
//                break;
//            case "WAITING":
//                result = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING, request);
//                break;
//            case "REJECTED":
//                result = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, request);
//                break;
//            default:
//                throw new WrongRequestException("Unknown state: UNSUPPORTED_STATUS");
//        }
//        return BookingMapper.toBookingDtoList(result);
//    }

    private void bookingDateValidation(Booking booking) {
        if (booking.getStart().isAfter(booking.getEnd()))
            throw new WrongRequestException("Время начала бронирования после времени окончания");
        if (booking.getStart().isEqual(booking.getEnd()))
            throw new WrongRequestException("Время начала бронирования равно времени окончания");
        if (booking.getStart().isBefore(LocalDateTime.now()))
            throw new WrongRequestException("Время начала бронирования не может быть в прошлом");
        if (booking.getEnd().isBefore(LocalDateTime.now()))
            throw new WrongRequestException("Время окончания бронирования не может быть в прошлом");
    }

//    private void bookingDateValidation(Booking booking) {
//        if (booking.getStart().isAfter(booking.getEnd())) {
//            throw new WrongRequestException("Start date is after end date");
//        }
//        if (booking.getStart().isEqual(booking.getEnd())) {
//            throw new WrongRequestException("Start date is equal end date");
//        }
//        if (booking.getStart().isBefore(LocalDateTime.now())) {
//            throw new WrongRequestException("Can not start in the past");
//        }
//        if (booking.getEnd().isBefore(LocalDateTime.now())) {
//            throw new WrongRequestException("Can not end in the past");
//        }
//    }
}
