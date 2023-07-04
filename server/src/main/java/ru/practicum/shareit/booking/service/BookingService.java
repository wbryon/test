package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, BookingDto bookingDto);

    BookingDto update(Long userId, Long bookingId, Boolean approved);

    BookingDto findBookingById(Long userId, Long bookingId);

    List<BookingDto> getAllBookingsByBooker(Long userId, String state, Integer from, Integer size);

    List<BookingDto> getAllBookingsByOwner(Long userId, String state, Integer from, Integer size);

    void delete(Long id);
}
