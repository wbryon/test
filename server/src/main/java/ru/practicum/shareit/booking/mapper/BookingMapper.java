package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

public class BookingMapper {

    public static Booking toBooking(BookingDto bookingDto, User booker, Item item) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(bookingDto.getStatus());
        return booking;
    }

    public static BookingDto toBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setItem(new ItemDto(booking.getItem().getId(), booking.getItem().getName()));
        bookingDto.setBooker(new UserDto(booking.getBooker().getId()));
        bookingDto.setStatus(booking.getStatus());
        return bookingDto;
    }

    public static BookingDto toBookingDtoShort(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        if (booking == null)
            bookingDto = null;
        else {
            bookingDto.setId(booking.getId());
            bookingDto.setBookerId(booking.getBooker().getId());
        }
        return bookingDto;
    }

    public static List<BookingDto> toBookingDtoList(List<Booking> bookings) {
        List<BookingDto> bookingDtoList = new ArrayList<>();
        for (Booking booking : bookings) {
            BookingDto bookingDto = toBookingDto(booking);
            bookingDtoList.add(bookingDto);
        }
        return bookingDtoList;
    }
}
