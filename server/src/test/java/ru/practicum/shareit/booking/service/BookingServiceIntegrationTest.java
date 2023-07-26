package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.WrongRequestException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BookingServiceIntegrationTest {

    @Autowired
    BookingService bookingService;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    UserService userService;
    @Autowired
    ItemService itemService;

    BookingDto bookingWithUnavailableItem;
    BookingDto bookingFromController;
    BookingDto bookingFromController2;
    Booking bookingFromDb;
    Booking bookingFromDb2;
    BookingDto createdBooking;
    BookingDto createdBooking2;
    Booking createdBooking3;
    UserDto createdOwner;
    UserDto createdBooker2;
    UserDto createdBooker;
    ItemDto createdItem;
    ItemDto createdItem2;

    @BeforeEach
    void beforeEach() {
        UserDto user = new UserDto();
        user.setName("testNameUser");
        user.setEmail("test@mail.com");
        createdOwner = userService.create(user);

        UserDto user2 = new UserDto();
        user2.setName("testNameUser2");
        user2.setEmail("test22@mail.com");
        createdBooker2 = userService.create(user2);

        UserDto booker = new UserDto();
        booker.setName("testBooker");
        booker.setEmail("testbooker@mail.com");
        createdBooker = userService.create(booker);

        ItemDto item = new ItemDto();
        item.setName("testItem");
        item.setDescription("testDescription");
        item.setAvailable(true);
        createdItem = itemService.create(createdOwner.getId(), item);

        ItemDto item2 = new ItemDto();
        item2.setName("testItem");
        item2.setDescription("testDescription");
        item2.setAvailable(false);
        createdItem2 = itemService.create(createdOwner.getId(), item2);

        LocalDateTime currentTime = LocalDateTime.now();

        bookingFromController = new BookingDto();
        bookingFromController.setItemId(createdItem.getId());
        bookingFromController.setStatus(Status.WAITING);
        bookingFromController.setStart(currentTime.plusDays(1));
        bookingFromController.setEnd(currentTime.plusDays(2));
        createdBooking = bookingService.create(createdBooker.getId(), bookingFromController);

        bookingFromController2 = new BookingDto();
        bookingFromController2.setItemId(createdItem.getId());
        bookingFromController2.setStatus(Status.REJECTED);
        bookingFromController2.setStart(currentTime.plusMonths(1));
        bookingFromController2.setEnd(currentTime.plusMonths(2));
        createdBooking2 = bookingService.create(createdBooker2.getId(), bookingFromController2);

        bookingFromDb = new Booking();
        bookingFromDb.setItem(ItemMapper.toItem(createdItem, UserMapper.toUser(createdOwner)));
        bookingFromDb.setStatus(Status.APPROVED);
        bookingFromDb.setStart(currentTime.minusDays(1));
        bookingFromDb.setEnd(currentTime.minusHours(2));
        bookingFromDb.setBooker(UserMapper.toUser(createdBooker2));
        bookingFromDb = bookingRepository.save(bookingFromDb);

        bookingFromDb2 = new Booking();
        bookingFromDb2.setItem(ItemMapper.toItem(createdItem, UserMapper.toUser(createdOwner)));
        bookingFromDb2.setStatus(Status.REJECTED);
        bookingFromDb2.setStart(currentTime.minusDays(1));
        bookingFromDb2.setEnd(currentTime.plusDays(2));
        bookingFromDb2.setBooker(UserMapper.toUser(createdBooker2));
        bookingFromDb2 = bookingRepository.save(bookingFromDb2);

        bookingWithUnavailableItem = new BookingDto();
        bookingWithUnavailableItem.setItemId(createdItem2.getId());
        bookingWithUnavailableItem.setStart(currentTime.plusDays(2));
        bookingWithUnavailableItem.setEnd(currentTime.plusDays(3));
    }

    @Test
    void shouldNotCreateIfItemIsUnavailable() {
        assertThrows(WrongRequestException.class, () -> bookingService.create(createdBooker2.getId(), bookingWithUnavailableItem));
    }

    @Test
    void shouldNotCreateIfBookerIsOwner() {
        assertThrows(NotFoundException.class, () -> bookingService.create(createdOwner.getId(), bookingWithUnavailableItem));
    }

    @Test
    void shouldFindBookingById() {
        BookingDto actual = bookingService.findBookingById(createdOwner.getId(), createdBooking.getId());

        assertNotNull(actual);
        assertEquals(createdBooking.getItem().getId(), actual.getItem().getId());
        assertEquals(createdBooking.getStart().withNano(0), actual.getStart().withNano(0));
        assertEquals(createdBooking.getEnd().withNano(0), actual.getEnd().withNano(0));
    }

    @Test
    void shouldNotFindBookingByIdIfUserIsNotOwnerOrBooker() {
        assertThrows(NotFoundException.class, () -> bookingService.findBookingById(createdBooker2.getId(), createdBooking.getId()));
    }

    @Test
    void shouldGetWaitingBookingsByOwner() {
        List<BookingDto> bookings = bookingService.getAllBookingsByOwner(createdOwner.getId(),
                "WAITING", 0, 10);

        assertNotNull(bookings);
        assertEquals(2, bookings.size());
        assertEquals(bookingFromController.getStatus(), bookings.get(0).getStatus());
    }

    @Test
    void shouldGetAllBookingsByOwner() {
        List<BookingDto> bookings = bookingService.getAllBookingsByOwner(createdOwner.getId(),
                "ALL", 0, 10);

        assertNotNull(bookings);
        assertEquals(4, bookings.size());
        assertEquals(createdBooking2.getId(), bookings.get(0).getId());
        assertEquals(createdBooking.getId(), bookings.get(1).getId());
    }

    @Test
    void shouldGetPastBookingsByOwner() {
        List<BookingDto> bookings = bookingService.getAllBookingsByOwner(createdOwner.getId(),
                "PAST", 0, 10);
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(bookingFromDb.getId(), bookings.get(0).getId());
    }

    @Test
    void shouldGetCurrentBookingsByOwner() {
        List<BookingDto> bookings = bookingService.getAllBookingsByOwner(createdOwner.getId(),
                "CURRENT", 0, 10);
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(bookingFromDb2.getId(), bookings.get(0).getId());
    }

    @Test
    void shouldGetFutureBookingsByOwner() {
        List<BookingDto> bookings = bookingService.getAllBookingsByOwner(createdOwner.getId(),
                "FUTURE", 0, 10);
        assertNotNull(bookings);
        assertEquals(2, bookings.size());
    }

    @Test
    void shouldGetRejectedBookingsByOwner() {
        List<BookingDto> bookings = bookingService.getAllBookingsByOwner(createdOwner.getId(),
                "REJECTED", 0, 10);
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(bookingFromDb2.getId(), bookings.get(0).getId());
    }

    @Test
    void shouldNotGetAllBookingsByOwnerWithUnknownState() {
        assertThrows(WrongRequestException.class, () -> bookingService.getAllBookingsByOwner(createdBooker.getId(),
                "someState", 0, 10));
    }

    @Test
    void shouldNotGetAllBookingsByBookerWithUnknownState() {

        assertThrows(WrongRequestException.class, () -> bookingService.getAllBookingsByBooker(createdBooker.getId(),
                "someState", 0, 10));
    }

    @Test
    void shouldGetWaitingBookingsByBooker() {
        List<BookingDto> bookings = bookingService.getAllBookingsByBooker(createdBooker.getId(),
                "WAITING", 0, 10);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(createdBooker.getId(), bookings.get(0).getBooker().getId());
        assertEquals(bookingFromController.getStatus(), bookings.get(0).getStatus());
    }

    @Test
    void shouldGetAllBookingsByBooker() {
        List<BookingDto> bookings = bookingService.getAllBookingsByBooker(createdBooker.getId(),
                "ALL", 0, 10);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(createdBooker.getId(), bookings.get(0).getBooker().getId());
        assertEquals(bookingFromController.getStatus(), bookings.get(0).getStatus());
    }

    @Test
    void shouldGetPastBookingsByBooker() {
        List<BookingDto> bookings = bookingService.getAllBookingsByBooker(createdBooker2.getId(),
                "PAST", 0, 10);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(bookingFromDb.getId(), bookings.get(0).getId());
    }

    @Test
    void shouldGetRejectedBookingsByBooker() {
        List<BookingDto> bookings = bookingService.getAllBookingsByBooker(createdBooker2.getId(),
                "REJECTED", 0, 10);
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(bookingFromDb2.getId(), bookings.get(0).getId());
    }

    @Test
    void shouldGetCurrentBookingsByBooker() {
        List<BookingDto> bookings = bookingService.getAllBookingsByBooker(createdBooker2.getId(),
                "CURRENT", 0, 10);
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(bookingFromDb2.getId(), bookings.get(0).getId());
    }

    @Test
    void shouldGetFutureBookingsByBooker() {
        List<BookingDto> bookings = bookingService.getAllBookingsByBooker(createdBooker.getId(),
                "FUTURE", 0, 10);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), createdBooking.getId());
    }

    @Test
    void shouldNotGetAllBookingsByBookerIfUnknownUser() {
        assertThrows(NotFoundException.class, () ->
                bookingService.getAllBookingsByBooker(999L, Status.WAITING.toString(), 0, 10));
    }

    @Test
    void shouldUpdateBooking() {
        BookingDto actual
                = bookingService.update(createdOwner.getId(), createdBooking.getId(), true);
        assertEquals(actual.getStatus(), Status.APPROVED);
    }

    @Test
    void shouldUpdateBookingIfNotApproved() {
        BookingDto actual
                = bookingService.update(createdOwner.getId(), createdBooking.getId(), false);
        assertEquals(actual.getStatus(), Status.REJECTED);
    }

    @Test
    void shouldNotUpdateBookingIfApprovedIsNull() {
        assertThrows(WrongRequestException.class,
                () -> bookingService.update(createdOwner.getId(), createdBooking.getId(), null));
    }

    @Test
    void shouldNotUpdateBookingIfUserIsNotOwner() {
        assertThrows(NotFoundException.class,
                () -> bookingService.update(createdBooker2.getId(), createdBooking.getId(), true));
    }

    @AfterEach
    void afterEach() {
        bookingService.delete(createdBooking.getId());
        bookingService.delete(createdBooking2.getId());
        bookingService.delete(bookingFromDb.getId());
        bookingService.delete(bookingFromDb2.getId());
        itemService.delete(createdOwner.getId(), createdItem.getId());
        itemService.delete(createdOwner.getId(), createdItem2.getId());
        userService.delete(createdOwner.getId());
        userService.delete(createdBooker.getId());
        userService.delete(createdBooker2.getId());

    }
}