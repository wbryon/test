package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase
class BookingRepositoryTest {
    User createdUser1;
    User createdUser2;
    Item createdItem1;
    Item createdItem2;
    Request createdRequest1;
    Request createdRequest2;
    Booking createdBooking1;
    Booking createdBooking2;
    Booking createdBooking3;
    Booking createdBooking4;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    public void beforeEach() {
        User user1 = new User();
        user1.setName("user1");
        user1.setEmail("user1@mail.ru");
        createdUser1 = userRepository.save(user1);

        User user2 = new User();
        user2.setName("user2");
        user2.setEmail("user2@mail.ru");
        createdUser2 = userRepository.save(user2);

        Item item1 = new Item();
        item1.setName("item1");
        item1.setDescription("description1");
        item1.setAvailable(true);
        item1.setOwner(createdUser1);
        createdItem1 = itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("item2");
        item2.setDescription("description2");
        item2.setAvailable(true);
        item2.setOwner(createdUser2);
        createdItem2 = itemRepository.save(item2);

        Request request1 = new Request();
        request1.setDescription("description1");
        request1.setRequestorId(user1.getId());
        request1.setCreated(LocalDateTime.now().minusDays(5));
        createdRequest1 = requestRepository.save(request1);

        Request request2 = new Request();
        request2.setDescription("description1");
        request2.setRequestorId(user1.getId());
        request2.setCreated(LocalDateTime.now().minusDays(2));
        createdRequest2 = requestRepository.save(request2);

        LocalDateTime currentTime = LocalDateTime.now();

        Booking booking1 = new Booking();
        booking1.setItem(createdItem1);
        booking1.setStatus(Status.WAITING);
        booking1.setStart(currentTime.plusDays(5));
        booking1.setEnd(currentTime.plusDays(7));
        booking1.setBooker(createdUser1);
        createdBooking1 = bookingRepository.save(booking1);

        Booking booking2 = new Booking();
        booking2.setItem(createdItem1);
        booking2.setStatus(Status.APPROVED);
        booking2.setStart(currentTime.minusDays(7));
        booking2.setEnd(currentTime.minusDays(5));
        booking2.setBooker(createdUser1);
        createdBooking2 = bookingRepository.save(booking2);

        Booking booking3 = new Booking();
        booking3.setItem(createdItem2);
        booking3.setStatus(Status.REJECTED);
        booking3.setStart(currentTime.minusDays(10));
        booking3.setEnd(currentTime.minusDays(8));
        booking3.setBooker(createdUser1);
        createdBooking3 = bookingRepository.save(booking3);

        Booking booking4 = new Booking();
        booking4.setItem(createdItem1);
        booking4.setStatus(Status.APPROVED);
        booking4.setStart(currentTime.minusDays(2));
        booking4.setEnd(currentTime.plusDays(3));
        booking4.setBooker(createdUser1);
        createdBooking4 = bookingRepository.save(booking4);
    }

    @Test
    void getByIdAndCheck_IsBookingValid() {
        assertEquals(createdBooking1, bookingRepository.findByBookingId(createdBooking1.getId()));
    }

    @Test
    void getByIdAndCheck_IsBookingInvalid() {
        assertThrows(NotFoundException.class, () -> bookingRepository.findByBookingId(42L));
    }

    @Test
    void getFirstByItemIdAndStatusAndStartDateIsBeforeOrderByStartDateDesc() {
        Booking actual = bookingRepository.findFirstByItemIdAndStatusAndStartIsBeforeOrderByStartDesc(createdItem1.getId(), Status.APPROVED, LocalDateTime.now());
        assertEquals(createdBooking4, actual);
    }

    @Test
    void getFirstByItemIdAndStatusAndStartDateIsAfterOrderByStartDateAsc() {
        Booking actual = bookingRepository.findFirstByItemIdAndStatusAndStartIsAfterOrderByStart(createdItem1.getId(), Status.WAITING, LocalDateTime.now());
        assertEquals(createdBooking1, actual);
    }

    @Test
    void getAllByEndDateBeforeAndStatusAndItemInOrderByStartDateDesc() {
        List<Item> itemList = new java.util.ArrayList<>();
        itemList.add(createdItem1);
        List<Booking> bookingList = bookingRepository.findAllByEndBeforeAndStatusAndItemInOrderByStartDesc(LocalDateTime.now(), Status.APPROVED, itemList);
        assertEquals(1, bookingList.size());
        assertTrue(bookingList.contains(createdBooking2));
    }

    @Test
    void getAllByStartDateAfterAndStatusAndItemInOrderByStartDateDesc() {
        List<Item> itemList = new java.util.ArrayList<>();
        itemList.add(createdItem1);
        List<Booking> bookingList = bookingRepository.findAllByStartAfterAndStatusAndItemInOrderByStartDesc(LocalDateTime.now(), Status.WAITING, itemList);
        assertEquals(1, bookingList.size());
        assertTrue(bookingList.contains(createdBooking1));
    }

    @Test
    void getAllByItem_OwnerIdOrderByStartDateDesc() {
        List<Booking> bookingList = bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(createdItem2.getOwner().getId(), PageRequest.of(0, 10));
        assertEquals(1, bookingList.size());
        assertTrue(bookingList.contains(createdBooking3));
    }

    @Test
    void getAllByItem_Owner_IdAndStartDateIsBeforeAndEndDateIsAfterOrderByStartDateDesc() {
        List<Booking> bookingList = bookingRepository.findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(createdItem1.getOwner().getId(), LocalDateTime.now(), LocalDateTime.now(), PageRequest.of(0, 10));
        assertEquals(1, bookingList.size());
        assertTrue(bookingList.contains(createdBooking4));
    }

    @Test
    void getAllByItem_OwnerIdAndEndDateIsBeforeOrderByStartDateDesc() {
        List<Booking> bookingList = bookingRepository.findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(createdItem1.getOwner().getId(), LocalDateTime.now(), LocalDateTime.now(), PageRequest.of(0, 10));
        assertEquals(1, bookingList.size());
        assertTrue(bookingList.contains(createdBooking4));
    }

    @Test
    void getAllByItem_OwnerIdAndStartDateIsAfterOrderByStartDateDesc() {
        List<Booking> bookingList = bookingRepository.findAllByItem_OwnerIdAndStartIsAfterOrderByStartDesc(createdItem1.getOwner().getId(), LocalDateTime.now(), PageRequest.of(0, 10));
        assertEquals(1, bookingList.size());
        assertTrue(bookingList.contains(createdBooking1));
    }

    @Test
    void getAllByItem_OwnerIdAndStatusOrderByStartDateDesc() {
        List<Booking> bookingList = bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(createdItem2.getOwner().getId(), Status.REJECTED, PageRequest.of(0, 10));
        assertEquals(1, bookingList.size());
        assertTrue(bookingList.contains(createdBooking3));
    }

    @Test
    void getAllByBookerIdOrderByStartDateDesc() {
        List<Booking> bookingList = bookingRepository.findAllByBookerIdOrderByStartDesc(createdUser1.getId(), PageRequest.of(0, 10));
        assertEquals(4, bookingList.size());
        assertTrue(bookingList.contains(createdBooking1));
        assertTrue(bookingList.contains(createdBooking2));
        assertTrue(bookingList.contains(createdBooking3));
        assertTrue(bookingList.contains(createdBooking4));
    }

    @Test
    void getAllByBookerIdAndItem_IdAndStatusAndEndDateIsBefore() {
        List<Booking> bookingList = bookingRepository.findAllByBookerIdAndItem_IdAndStatusAndEndIsBefore(createdUser1.getId(), createdItem2.getId(), Status.REJECTED, LocalDateTime.now());
        assertEquals(1, bookingList.size());
        assertTrue(bookingList.contains(createdBooking3));
    }

    @Test
    void getAllByBookerIdAndStartDateIsAfterOrderByStartDateDesc() {
        List<Booking> bookingList = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(createdUser1.getId(), LocalDateTime.now(), PageRequest.of(0, 10));
        assertEquals(1, bookingList.size());
        assertTrue(bookingList.contains(createdBooking1));
    }

    @Test
    void getAllByBookerIdAndEndDateIsBeforeOrderByStartDateDesc() {
        List<Booking> bookingList = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(createdUser1.getId(), LocalDateTime.now(), PageRequest.of(0, 10));
        assertEquals(2, bookingList.size());
        assertTrue(bookingList.contains(createdBooking2));
        assertTrue(bookingList.contains(createdBooking3));
    }

    @Test
    void getAllByBookerIdAndStartDateIsBeforeAndEndDateIsAfterOrderByStartDateDesc() {
        List<Booking> bookingList = bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStart(createdUser1.getId(), LocalDateTime.now(), LocalDateTime.now(), PageRequest.of(0, 10));
        assertEquals(1, bookingList.size());
        assertTrue(bookingList.contains(createdBooking4));
    }

    @Test
    void getAllByBookerIdAndStatusOrderByStartDateDesc() {
        List<Booking> bookingList = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(createdUser1.getId(), Status.APPROVED, PageRequest.of(0, 10));
        assertEquals(2, bookingList.size());
        assertTrue(bookingList.contains(createdBooking2));
        assertTrue(bookingList.contains(createdBooking4));
    }
}

//@DataJpaTest
//@AutoConfigureTestDatabase
//class BookingRepositoryTest {
//    User createdUser1;
//    User createdUser2;
//    Item createdItem1;
//    Item createdItem2;
//    Request createdRequest1;
//    Request createdRequest2;
//    Booking createdBooking1;
//    Booking createdBooking2;
//    Booking createdBooking3;
//    Booking createdBooking4;
//    @Autowired
//    private ItemRepository itemRepository;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private RequestRepository requestRepository;
//    @Autowired
//    private BookingRepository bookingRepository;
//
//    @BeforeEach
//    public void beforeEach() {
//        User user1 = new User();
//        user1.setName("user1");
//        user1.setEmail("user1@mail.com");
//        createdUser1 = userRepository.save(user1);
//
//        User user2 = new User();
//        user2.setName("user2");
//        user2.setEmail("user2@mail.com");
//        createdUser2 = userRepository.save(user2);
//
//        Item item1 = new Item();
//        item1.setName("testItem");
//        item1.setDescription("testItemDescription");
//        item1.setAvailable(true);
//        item1.setOwner(createdUser1);
//        createdItem1 = itemRepository.save(item1);
//
//        Item item2 = new Item();
//        item2.setName("testItem2");
//        item2.setDescription("testItem2Description");
//        item2.setAvailable(true);
//        item2.setOwner(createdUser2);
//        createdItem2 = itemRepository.save(item2);
//
//        Request request1 = new Request();
//        request1.setDescription("testDescription1");
//        request1.setRequestorId(user1.getId());
//        request1.setCreated(LocalDateTime.now().minusDays(5));
//        createdRequest1 = requestRepository.save(request1);
//
//        Request request2 = new Request();
//        request2.setDescription("testDescription1");
//        request2.setRequestorId(user1.getId());
//        request2.setCreated(LocalDateTime.now().minusDays(2));
//        createdRequest2 = requestRepository.save(request2);
//
//        LocalDateTime currentTime = LocalDateTime.now();
//
//        Booking booking1 = new Booking();
//        booking1.setItem(createdItem1);
//        booking1.setStatus(Status.WAITING);
//        booking1.setStart(currentTime.plusDays(5));
//        booking1.setEnd(currentTime.plusDays(7));
//        booking1.setBooker(createdUser1);
//        createdBooking1 = bookingRepository.save(booking1);
//
//        Booking booking2 = new Booking();
//        booking2.setItem(createdItem1);
//        booking2.setStatus(Status.APPROVED);
//        booking2.setStart(currentTime.minusDays(7));
//        booking2.setEnd(currentTime.minusDays(5));
//        booking2.setBooker(createdUser1);
//        createdBooking2 = bookingRepository.save(booking2);
//
//        Booking booking3 = new Booking();
//        booking3.setItem(createdItem2);
//        booking3.setStatus(Status.REJECTED);
//        booking3.setStart(currentTime.minusDays(10));
//        booking3.setEnd(currentTime.minusDays(8));
//        booking3.setBooker(createdUser1);
//        createdBooking3 = bookingRepository.save(booking3);
//
//        Booking booking4 = new Booking();
//        booking4.setItem(createdItem1);
//        booking4.setStatus(Status.APPROVED);
//        booking4.setStart(currentTime.minusDays(2));
//        booking4.setEnd(currentTime.plusDays(3));
//        booking4.setBooker(createdUser1);
//        createdBooking4 = bookingRepository.save(booking4);
//    }
//
//    @Test
//    void getByIdAndCheck_IsBookingValid() {
//        assertEquals(createdBooking1, bookingRepository.findByBookingId(createdBooking1.getId()));
//    }
//
//    @Test
//    void getByIdAndCheck_IsBookingInvalid() {
//        assertThrows(NotFoundException.class, () -> bookingRepository.findByBookingId(999L));
//    }
//
//    @Test
//    void getFirstByItemIdAndStatusAndStartDateIsBeforeOrderByStartDateDesc() {
//        Booking actual = bookingRepository.findFirstByItemIdAndStatusAndStartIsBeforeOrderByStartDesc(createdItem1.getId(), Status.APPROVED, LocalDateTime.now());
//        assertEquals(createdBooking4, actual);
//    }
//
//    @DirtiesContext
//    @Test
//    void getFirstByItemIdAndStatusAndStartDateIsAfterOrderByStartDateAsc() {
//        Booking actual = bookingRepository.findFirstByItemIdAndStatusAndStartIsAfterOrderByStart(createdItem1.getId(), Status.WAITING, LocalDateTime.now());
//        assertEquals(createdBooking1, actual);
//    }
//
//    @Test
//    void getAllByEndDateBeforeAndStatusAndItemInOrderByStartDateDesc() {
//        List<Booking> bookingList = bookingRepository.findAllByEndBeforeAndStatusAndItemInOrderByStartDesc(LocalDateTime.now(), Status.APPROVED, List.of(createdItem1));
//        assertEquals(1, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking2));
//    }
//
//    @Test
//    void getAllByStartDateAfterAndStatusAndItemInOrderByStartDateDesc() {
//        List<Booking> bookingList = bookingRepository.findAllByStartAfterAndStatusAndItemInOrderByStartDesc(LocalDateTime.now(), Status.WAITING, List.of(createdItem1));
//        assertEquals(1, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking1));
//    }
//
//    @Test
//    void getAllByItem_OwnerIdOrderByStartDateDesc() {
//        List<Booking> bookingList = bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(createdItem2.getOwner().getId(), PageRequest.of(0, 10));
//        assertEquals(1, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking3));
//    }
//
//    @Test
//    void getAllByItem_Owner_IdAndStartDateIsBeforeAndEndDateIsAfterOrderByStartDateDesc() {
//        List<Booking> bookingList = bookingRepository.findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(createdItem1.getOwner().getId(), LocalDateTime.now(), LocalDateTime.now(), PageRequest.of(0, 10));
//        assertEquals(1, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking4));
//    }
//
//    @Test
//    void getAllByItem_OwnerIdAndEndDateIsBeforeOrderByStartDateDesc() {
//        List<Booking> bookingList = bookingRepository.findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(createdItem1.getOwner().getId(), LocalDateTime.now(), LocalDateTime.now(), PageRequest.of(0, 10));
//        assertEquals(1, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking4));
//    }
//
//    @Test
//    void getAllByItem_OwnerIdAndStartDateIsAfterOrderByStartDateDesc() {
//        List<Booking> bookingList = bookingRepository.findAllByItem_OwnerIdAndStartIsAfterOrderByStartDesc(createdItem1.getOwner().getId(), LocalDateTime.now(), PageRequest.of(0, 10));
//        assertEquals(1, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking1));
//    }
//
//    @Test
//    void getAllByItem_OwnerIdAndStatusOrderByStartDateDesc() {
//        List<Booking> bookingList = bookingRepository.findAllByItem_OwnerIdAndStatusOrderByStartDesc(createdItem2.getOwner().getId(), Status.REJECTED, PageRequest.of(0, 10));
//        assertEquals(1, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking3));
//    }
//
//    @Test
//    void getAllByBookerIdOrderByStartDateDesc() {
//        List<Booking> bookingList = bookingRepository.findAllByBookerIdOrderByStartDesc(createdUser1.getId(), PageRequest.of(0, 10));
//        assertEquals(4, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking1));
//        assertTrue(bookingList.contains(createdBooking2));
//        assertTrue(bookingList.contains(createdBooking3));
//        assertTrue(bookingList.contains(createdBooking4));
//    }
//
//    @Test
//    void getAllByBookerIdAndItem_IdAndStatusAndEndDateIsBefore() {
//        List<Booking> bookingList = bookingRepository.findAllByBookerIdAndItem_IdAndStatusAndEndIsBefore(createdUser1.getId(), createdItem2.getId(), Status.REJECTED, LocalDateTime.now());
//        assertEquals(1, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking3));
//    }
//
//    @Test
//    void getAllByBookerIdAndStartDateIsAfterOrderByStartDateDesc() {
//        List<Booking> bookingList = bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(createdUser1.getId(), LocalDateTime.now(), PageRequest.of(0, 10));
//        assertEquals(1, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking1));
//    }
//
//    @Test
//    void getAllByBookerIdAndEndDateIsBeforeOrderByStartDateDesc() {
//        List<Booking> bookingList = bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(createdUser1.getId(), LocalDateTime.now(), PageRequest.of(0, 10));
//        assertEquals(2, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking2));
//        assertTrue(bookingList.contains(createdBooking3));
//    }
//
//    @Test
//    void getAllByBookerIdAndStartDateIsBeforeAndEndDateIsAfterOrderByStartDateDesc() {
//        List<Booking> bookingList = bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStart(createdUser1.getId(), LocalDateTime.now(), LocalDateTime.now(), PageRequest.of(0, 10));
//        assertEquals(1, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking4));
//    }
//
//    @Test
//    void getAllByBookerIdAndStatusOrderByStartDateDesc() {
//        List<Booking> bookingList = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(createdUser1.getId(), Status.APPROVED, PageRequest.of(0, 10));
//        assertEquals(2, bookingList.size());
//        assertTrue(bookingList.contains(createdBooking2));
//        assertTrue(bookingList.contains(createdBooking4));
//    }
//}