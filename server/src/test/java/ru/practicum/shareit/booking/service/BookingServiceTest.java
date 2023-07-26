package ru.practicum.shareit.booking.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void getById() {
    }

    @Test
    void shouldCreateBooking() {
        User owner = new User();
        owner.setId(2L);
        owner.setName("owner");
        owner.setEmail("owner@mail.ru");

        User booker = new User();
        booker.setId(1L);
        booker.setName("booker");
        booker.setEmail("booker@mail.ru");

        Item item = new Item();
        item.setId(1L);
        item.setName("item");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        when(itemRepository.findByItemId(any())).thenReturn(item);
        when(userRepository.findByUserId(any())).thenReturn(booker);
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto bookingDto = bookingService.create(booker.getId(), BookingMapper.toBookingDto(booking));
        assertEquals(bookingDto.getId(), booking.getId());
        assertEquals(bookingDto.getBooker().getId(), booking.getBooker().getId());
        assertEquals(bookingDto.getItem().getId(), booking.getItem().getId());
        assertEquals(bookingDto.getStart(), booking.getStart());
        assertEquals(bookingDto.getEnd(), booking.getEnd());

        verify(itemRepository).findByItemId(any());
        verify(userRepository).findByUserId(any());
        verify(bookingRepository).save(any());
    }

    @Test
    void shouldNotCreateIfBookerIsOwner() {
        User booker = new User();
        booker.setId(1L);
        booker.setName("booker");
        booker.setEmail("booker@mail.ru");

        Item item = new Item();
        item.setId(1L);
        item.setName("item");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(booker);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        when(itemRepository.findByItemId(any())).thenReturn(item);
        when(userRepository.findByUserId(any())).thenReturn(booker);

        assertThrows(NotFoundException.class, () -> bookingService.create(booker.getId(), BookingMapper.toBookingDto(booking)));
        verify(itemRepository).findByItemId(any());
        verify(userRepository).findByUserId(any());
    }

    @Test
    void shouldNotCreateIfItemIsUnavailable() {
        User owner = new User();
        owner.setId(2L);
        owner.setName("owner");
        owner.setEmail("owner@mail.ru");
        User booker = new User();
        booker.setId(1L);
        booker.setName("booker");
        booker.setEmail("booker@mail.ru");
        Item item = new Item();
        item.setId(1L);
        item.setName("item");
        item.setDescription("description");
        item.setAvailable(false);
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        when(itemRepository.findByItemId(any())).thenReturn(item);
        when(userRepository.findByUserId(any())).thenReturn(booker);
        assertThrows(WrongRequestException.class, () -> bookingService.create(booker.getId(), BookingMapper.toBookingDto(booking)));
        verify(itemRepository).findByItemId(any());
        verify(userRepository).findByUserId(any());
    }

    @Test
    void shouldUpdateBooking() {
        User owner = new User();
        owner.setId(2L);
        owner.setName("owner");
        owner.setEmail("owner@mail.ru");

        User booker = new User();
        booker.setId(1L);
        booker.setName("booker");
        booker.setEmail("booker@mail.ru");

        Item item = new Item();
        item.setId(1L);
        item.setName("item");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        when(bookingRepository.findByBookingId(1L)).thenReturn(booking);
        when(userRepository.findByUserId(2L)).thenReturn(owner);
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto bookingResponseDto = bookingService.update(2L, 1L, true);
        assertEquals(bookingResponseDto.getStatus(), Status.APPROVED);
    }

    @Test
    void shouldNotUpdateIfAlreadyApproved() {
        User owner = new User();
        owner.setId(2L);
        owner.setName("owner");
        owner.setEmail("owner@mail.ru");

        User booker = new User();
        booker.setId(1L);
        booker.setName("booker");
        booker.setEmail("booker@mail.ru");

        Item item = new Item();
        item.setId(1L);
        item.setName("item");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(Status.APPROVED);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        when(bookingRepository.findByBookingId(1L)).thenReturn(booking);
        when(userRepository.findByUserId(2L)).thenReturn(owner);
        assertThrows(WrongRequestException.class, () -> bookingService.update(2L, 1L, true));
    }

    @Test
    void shouldNotUpdateIfStatusNull() {
        User owner = new User();
        owner.setId(2L);
        owner.setName("owner");
        owner.setEmail("owner@mail.ru");

        User booker = new User();
        booker.setId(1L);
        booker.setName("booker");
        booker.setEmail("booker@mail.ru");

        Item item = new Item();
        item.setId(1L);
        item.setName("item");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        when(bookingRepository.findByBookingId(1L)).thenReturn(booking);
        when(userRepository.findByUserId(2L)).thenReturn(owner);
        assertThrows(WrongRequestException.class, () -> bookingService.update(2L, 1L, null));
    }

    @Test
    void shouldGetAllBookingsByOwner() {
        User owner = new User();
        owner.setId(2L);
        owner.setName("owner");
        owner.setEmail("owner@mail.ru");

        User booker = new User();
        booker.setId(1L);
        booker.setName("booker");
        booker.setEmail("booker@mail.ru");

        Item item = new Item();
        item.setId(1L);
        item.setName("item");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        when(userRepository.findByUserId(2L)).thenReturn(owner);
        ArrayList<Booking> bookingsForResponse = new ArrayList<>();
        bookingsForResponse.add(booking);
        when(bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(any(), any())).thenReturn(bookingsForResponse);

        List<BookingDto> bookingResponseDtoList = bookingService.getAllBookingsByOwner(owner.getId(), "ALL", 0, 10);
        assertFalse(bookingResponseDtoList.isEmpty());
        assertEquals(bookingResponseDtoList.get(0).getId(), booking.getId());
        verify(bookingRepository).findAllByItem_OwnerIdOrderByStartDesc(any(), any());
    }

    @Test
    void shouldNotGetAllBookingsByOwnerWhenStatusIsUnsupported() {
        User owner = new User();
        owner.setId(2L);
        owner.setName("owner");
        owner.setEmail("owner@mail.ru");

        assertThrows(WrongRequestException.class, () -> bookingService.getAllBookingsByOwner(owner.getId(), "unknown", 0, 10));
    }

    @Test
    void shouldGetAllBookingsByBooker() {
        User owner = new User();
        owner.setId(2L);
        owner.setName("owner");
        owner.setEmail("owner@mail.ru");

        User booker = new User();
        booker.setId(1L);
        booker.setName("booker");
        booker.setEmail("booker@mail.ru");

        Item item = new Item();
        item.setId(1L);
        item.setName("item");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(any(), any())).thenReturn(bookings);
        List<BookingDto> bookingDtoList = bookingService.getAllBookingsByBooker(1L, "ALL", 0, 10);
        assertFalse(bookingDtoList.isEmpty());
        assertEquals(bookingDtoList.get(0).getId(), booking.getId());
        verify(bookingRepository).findAllByBookerIdOrderByStartDesc(any(), any());
    }

    @Test
    void shouldNotGetAllBookingsByBookerWhenStatusIsUnsupported() {
        User booker = new User();
        booker.setId(1L);
        booker.setName("booker");
        booker.setEmail("booker@mail.ru");

        assertThrows(WrongRequestException.class, () -> bookingService.getAllBookingsByBooker(booker.getId(), "unknown", 0, 10));
    }
}

//@ExtendWith(MockitoExtension.class)
//class BookingServiceTest {
//    @Mock
//    private BookingRepository bookingRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private ItemRepository itemRepository;
//
//    @InjectMocks
//    private BookingServiceImpl bookingService;
//
//    @Test
//    void getById() {
//    }
//
//    @Test
//    void create_isValid() {
//        User ownerOfItem = new User();
//        ownerOfItem.setId(2L);
//        ownerOfItem.setName("testNameUser2");
//        ownerOfItem.setEmail("test22@mail.com");
//
//        User booker = new User();
//        booker.setId(1L);
//        booker.setName("testBooker");
//        booker.setEmail("testbooker@mail.com");
//
//        Item item = new Item();
//        item.setId(1L);
//        item.setName("testItem");
//        item.setDescription("testDescription");
//        item.setAvailable(true);
//        item.setOwner(ownerOfItem);
//
//        Booking booking = new Booking();
//        booking.setId(1L);
//        booking.setBooker(booker);
//        booking.setItem(item);
//        booking.setStart(LocalDateTime.now().plusHours(1));
//        booking.setEnd(LocalDateTime.now().plusDays(1));
//
//        when(itemRepository.findByItemId(any())).thenReturn(item);
//        when(userRepository.findByUserId(any())).thenReturn(booker);
//        when(bookingRepository.save(any())).thenReturn(booking);
//
//        BookingDto bookingDtoForReturn = bookingService.create(booker.getId(), BookingMapper.toBookingDto(booking));
//        assertEquals(bookingDtoForReturn.getId(), booking.getId());
//        assertEquals(bookingDtoForReturn.getBooker().getId(), booking.getBooker().getId());
//        assertEquals(bookingDtoForReturn.getItem().getId(), booking.getItem().getId());
//        assertEquals(bookingDtoForReturn.getStart(), booking.getStart());
//        assertEquals(bookingDtoForReturn.getEnd(), booking.getEnd());
//
//        verify(itemRepository).findByItemId(any());
//        verify(userRepository).findByUserId(any());
//        verify(bookingRepository).save(any());
//    }
//
//    @Test
//    void create_BookerIsOwner() {
//        User booker = new User();
//        booker.setId(1L);
//        booker.setName("testBooker");
//        booker.setEmail("testbooker@mail.com");
//
//        Item item = new Item();
//        item.setId(1L);
//        item.setName("testItem");
//        item.setDescription("testDescription");
//        item.setAvailable(true);
//        item.setOwner(booker);
//
//        Booking booking = new Booking();
//        booking.setId(1L);
//        booking.setBooker(booker);
//        booking.setItem(item);
//        booking.setStatus(Status.WAITING);
//        booking.setStart(LocalDateTime.now().plusHours(1));
//        booking.setEnd(LocalDateTime.now().plusDays(1));
//
//        when(itemRepository.findByItemId(any())).thenReturn(item);
//        when(userRepository.findByUserId(any())).thenReturn(booker);
//
//        assertThrows(NotFoundException.class, () -> bookingService.create(booker.getId(), BookingMapper.toBookingDto(booking)));
//        verify(itemRepository).findByItemId(any());
//        verify(userRepository).findByUserId(any());
//    }
//
//    @Test
//    void create_ItemIsUnavailable() {
//        User ownerOfItem = new User();
//        ownerOfItem.setId(2L);
//        ownerOfItem.setName("testNameUser2");
//        ownerOfItem.setEmail("test22@mail.com");
//
//        User booker = new User();
//        booker.setId(1L);
//        booker.setName("testBooker");
//        booker.setEmail("testbooker@mail.com");
//
//        Item item = new Item();
//        item.setId(1L);
//        item.setName("testItem");
//        item.setDescription("testDescription");
//        item.setAvailable(false);
//        item.setOwner(ownerOfItem);
//
//        Booking booking = new Booking();
//        booking.setId(1L);
//        booking.setBooker(booker);
//        booking.setItem(item);
//        booking.setStart(LocalDateTime.now().plusHours(1));
//        booking.setEnd(LocalDateTime.now().plusDays(1));
//
//        when(itemRepository.findByItemId(any())).thenReturn(item);
//        when(userRepository.findByUserId(any())).thenReturn(booker);
//
//
//        assertThrows(WrongRequestException.class, () -> bookingService.create(booker.getId(), BookingMapper.toBookingDto(booking)));
//        verify(itemRepository).findByItemId(any());
//        verify(userRepository).findByUserId(any());
//    }
//
//    @Test
//    void approvingByOwner_isApproved() {
//        User ownerOfItem = new User();
//        ownerOfItem.setId(2L);
//        ownerOfItem.setName("testNameUser2");
//        ownerOfItem.setEmail("test22@mail.com");
//
//        User booker = new User();
//        booker.setId(1L);
//        booker.setName("testBooker");
//        booker.setEmail("testbooker@mail.com");
//
//        Item item = new Item();
//        item.setId(1L);
//        item.setName("testItem");
//        item.setDescription("testDescription");
//        item.setAvailable(true);
//        item.setOwner(ownerOfItem);
//
//        Booking booking = new Booking();
//        booking.setId(1L);
//        booking.setBooker(booker);
//        booking.setItem(item);
//        booking.setStatus(Status.WAITING);
//        booking.setStart(LocalDateTime.now().plusHours(1));
//        booking.setEnd(LocalDateTime.now().plusDays(1));
//
//        when(bookingRepository.findByBookingId(1L)).thenReturn(booking);
//        when(userRepository.findByUserId(2L)).thenReturn(ownerOfItem);
//        when(bookingRepository.save(any())).thenReturn(booking);
//
//        BookingDto bookingResponseDto = bookingService.update(2L, 1L, true);
//        assertEquals(bookingResponseDto.getStatus(), Status.APPROVED);
//    }
//
//    @Test
//    void approvingByOwner_isAlreadyApproved() {
//        User ownerOfItem = new User();
//        ownerOfItem.setId(2L);
//        ownerOfItem.setName("testNameUser2");
//        ownerOfItem.setEmail("test22@mail.com");
//
//        User booker = new User();
//        booker.setId(1L);
//        booker.setName("testBooker");
//        booker.setEmail("testbooker@mail.com");
//
//        Item item = new Item();
//        item.setId(1L);
//        item.setName("testItem");
//        item.setDescription("testDescription");
//        item.setAvailable(true);
//        item.setOwner(ownerOfItem);
//
//        Booking booking = new Booking();
//        booking.setId(1L);
//        booking.setBooker(booker);
//        booking.setItem(item);
//        booking.setStatus(Status.APPROVED);
//        booking.setStart(LocalDateTime.now().plusHours(1));
//        booking.setEnd(LocalDateTime.now().plusDays(1));
//
//        when(bookingRepository.findByBookingId(1L)).thenReturn(booking);
//        when(userRepository.findByUserId(2L)).thenReturn(ownerOfItem);
//        assertThrows(WrongRequestException.class, () -> bookingService.update(2L, 1L, true));
//    }
//
//    @Test
//    void approvingByOwner_isStatusNull() {
//        User ownerOfItem = new User();
//        ownerOfItem.setId(2L);
//        ownerOfItem.setName("testNameUser2");
//        ownerOfItem.setEmail("test22@mail.com");
//
//        User booker = new User();
//        booker.setId(1L);
//        booker.setName("testBooker");
//        booker.setEmail("testbooker@mail.com");
//
//        Item item = new Item();
//        item.setId(1L);
//        item.setName("testItem");
//        item.setDescription("testDescription");
//        item.setAvailable(true);
//        item.setOwner(ownerOfItem);
//
//        Booking booking = new Booking();
//        booking.setId(1L);
//        booking.setBooker(booker);
//        booking.setItem(item);
//        booking.setStatus(Status.WAITING);
//        booking.setStart(LocalDateTime.now().plusHours(1));
//        booking.setEnd(LocalDateTime.now().plusDays(1));
//
//        when(bookingRepository.findByBookingId(1L)).thenReturn(booking);
//        when(userRepository.findByUserId(2L)).thenReturn(ownerOfItem);
//        assertThrows(WrongRequestException.class, () -> bookingService.update(2L, 1L, null));
//    }
//
//    @Test
//    void getAllByOwner_ALL() {
//        User ownerOfItem = new User();
//        ownerOfItem.setId(2L);
//        ownerOfItem.setName("testNameUser2");
//        ownerOfItem.setEmail("test22@mail.com");
//
//        User booker = new User();
//        booker.setId(1L);
//        booker.setName("testBooker");
//        booker.setEmail("testbooker@mail.com");
//
//        Item item = new Item();
//        item.setId(1L);
//        item.setName("testItem");
//        item.setDescription("testDescription");
//        item.setAvailable(true);
//        item.setOwner(ownerOfItem);
//
//        Booking booking = new Booking();
//        booking.setId(1L);
//        booking.setBooker(booker);
//        booking.setItem(item);
//        booking.setStart(LocalDateTime.now().plusHours(1));
//        booking.setEnd(LocalDateTime.now().plusDays(1));
//
//        when(userRepository.findByUserId(2L)).thenReturn(ownerOfItem);
//        ArrayList<Booking> bookingsForResponse = new ArrayList<>();
//        bookingsForResponse.add(booking);
//        when(bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(any(), any())).thenReturn(bookingsForResponse);
//
//        List<BookingDto> bookingResponseDtoList = bookingService.getAllBookingsByOwner(ownerOfItem.getId(), "ALL", 0, 10);
//        assertFalse(bookingResponseDtoList.isEmpty());
//        assertEquals(bookingResponseDtoList.get(0).getId(), booking.getId());
//        verify(bookingRepository).findAllByItem_OwnerIdOrderByStartDesc(any(), any());
//    }
//
//    @Test
//    void getAllByOwner_UNSUPPORTED_STATUS() {
//        User ownerOfItem = new User();
//        ownerOfItem.setId(2L);
//        ownerOfItem.setName("testNameUser2");
//        ownerOfItem.setEmail("test22@mail.com");
//
//        assertThrows(WrongRequestException.class, () -> bookingService.getAllBookingsByOwner(ownerOfItem.getId(), "blabla", 0, 10));
//    }
//
//    @Test
//    void getAllByBooker() {
//        User ownerOfItem = new User();
//        ownerOfItem.setId(2L);
//        ownerOfItem.setName("testNameUser2");
//        ownerOfItem.setEmail("test22@mail.com");
//
//        User booker = new User();
//        booker.setId(1L);
//        booker.setName("testBooker");
//        booker.setEmail("testbooker@mail.com");
//
//        Item item = new Item();
//        item.setId(1L);
//        item.setName("testItem");
//        item.setDescription("testDescription");
//        item.setAvailable(true);
//        item.setOwner(ownerOfItem);
//
//        Booking booking = new Booking();
//        booking.setId(1L);
//        booking.setBooker(booker);
//        booking.setItem(item);
//        booking.setStart(LocalDateTime.now().plusHours(1));
//        booking.setEnd(LocalDateTime.now().plusDays(1));
//
//        ArrayList<Booking> bookingsForResponse = new ArrayList<>();
//        bookingsForResponse.add(booking);
//        when(bookingRepository.findAllByBookerIdOrderByStartDesc(any(), any())).thenReturn(bookingsForResponse);
//        List<BookingDto> bookingResponseDtoList = bookingService.getAllBookingsByBooker(1L, "ALL", 0, 10);
//        assertFalse(bookingResponseDtoList.isEmpty());
//        assertEquals(bookingResponseDtoList.get(0).getId(), booking.getId());
//        verify(bookingRepository).findAllByBookerIdOrderByStartDesc(any(), any());
//    }
//
//    @Test
//    void getAllByBooker_UNSUPPORTED_STATUS() {
//        User booker = new User();
//        booker.setId(1L);
//        booker.setName("testBooker");
//        booker.setEmail("testbooker@mail.com");
//
//        assertThrows(WrongRequestException.class, () -> bookingService.getAllBookingsByBooker(booker.getId(), "blabla", 0, 10));
//    }
//}