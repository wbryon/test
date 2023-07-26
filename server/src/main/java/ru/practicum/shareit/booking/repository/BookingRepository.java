package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    default Booking findByBookingId(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Бронирование с id = " + id + " не найдено"));
    }

    Booking findFirstByItemIdAndStatusAndStartIsBeforeOrderByStartDesc(Long itemId, Status bookingStatus, LocalDateTime currentTime);

    Booking findFirstByItemIdAndStatusAndStartIsAfterOrderByStart(Long itemId, Status bookingStatus, LocalDateTime currentTime);

    List<Booking> findAllByEndBeforeAndStatusAndItemInOrderByStartDesc(LocalDateTime currentTime, Status bookingStatus, List<Item> items);

    List<Booking> findAllByStartAfterAndStatusAndItemInOrderByStartDesc(LocalDateTime currentTime, Status bookingStatus, List<Item> items);

    List<Booking> findAllByItem_OwnerIdOrderByStartDesc(Long ownerId, PageRequest of);

    List<Booking> findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long ownerId, LocalDateTime currentTimeForStart, LocalDateTime currentTimeForEnd, PageRequest of);

    List<Booking> findAllByItem_OwnerIdAndEndIsBeforeOrderByStartDesc(long ownerId, LocalDateTime now, PageRequest of);

    List<Booking> findAllByItem_OwnerIdAndStartIsAfterOrderByStartDesc(Long ownerId, LocalDateTime currentTime, PageRequest of);

    List<Booking> findAllByItem_OwnerIdAndStatusOrderByStartDesc(Long ownerId, Status bookingStatus, PageRequest of);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findAllByBookerIdAndItem_IdAndStatusAndEndIsBefore(Long bookerId, Long itemId, Status bookingStatus, LocalDateTime currentTime);

    List<Booking> findAllByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime currentDate, PageRequest of);

    List<Booking> findAllByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime currentDate, PageRequest of);

    List<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStart(Long bookerId, LocalDateTime currentTimeForStart, LocalDateTime currentTimeForEnd, PageRequest of);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status bookingStatus, PageRequest of);
}
