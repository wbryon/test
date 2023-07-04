package ru.practicum.shareit.item.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.WrongRequestException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;

    public ItemServiceImpl(UserRepository userRepository, ItemRepository itemRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository,
                           RequestRepository requestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User user = userRepository.findByUserId(userId);
        Item item = ItemMapper.toItem(itemDto, user);
        Request request = null;
        if (itemDto.getRequestId() != null)
            request = requestRepository.findById(itemDto.getRequestId()).orElse(new Request());
        item.setRequest(request);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item itemFromDb = itemRepository.findByItemId(itemId);
        User userFromDb = userRepository.findByUserId(userId);
        Item item = ItemMapper.toItem(itemDto, userFromDb);
        if (item.getName() == null)
            item.setName(itemFromDb.getName());
        if (item.getDescription() == null)
            item.setDescription(itemFromDb.getDescription());
        if (item.getAvailable() == null)
            item.setAvailable(itemFromDb.getAvailable());
        item.setId(itemId);
        item.setOwner(userFromDb);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public List<ItemDto> getAllOwnerItems(Long userId, Integer from, Integer size) {
        User owner = userRepository.findByUserId(userId);
        List<Item> items = itemRepository.findByOwnerIdOrderById(owner.getId(), PageRequest.of(from / size, size));
        return findItemDtoWithNextAndLastBookings(items);
    }

    @Override
    public ItemDto findItemById(Long userId, Long itemId) {
        Item item = itemRepository.findByItemId(itemId);
        ItemDto itemDto = ItemMapper.toItemDto(itemRepository.findByItemId(itemId));
        itemDto.setComments(commentRepository.getAllByItemId(itemId));
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime time = LocalDateTime.now();
            itemDto.setLastBooking(getLastBooking(itemId, time));
            itemDto.setNextBooking(getNextBooking(itemId, time));
        }
        return itemDto;
    }

    @Override
    public List<ItemDto> findItemForRental(String text) {
        List<ItemDto> listOfFoundItems = new ArrayList<>();
        if (text.isBlank())
            return listOfFoundItems;
        for (Item item : itemRepository.findItemForRental(text))
            listOfFoundItems.add(ItemMapper.toItemDto(item));
        return listOfFoundItems;
    }

    @Override
    public void delete(Long userId, Long itemId) {
        User user = userRepository.findByUserId(userId);
        Item item = itemRepository.findByItemId(itemId);
        if (user.getId().equals(item.getOwner().getId()))
            itemRepository.deleteById(itemId);
        else
            throw new WrongRequestException("Пользователь с id: " + userId + " не является владельцем вещи с id: " + itemId);

    }

    @Override
    public CommentDto createComment(CommentDto commentDto, Long userId, Long itemId) {
        Item item = itemRepository.findByItemId(itemId);
        User user = userRepository.findByUserId(userId);
        LocalDateTime timeOfCreation = LocalDateTime.now();
        commentDto.setCreated(timeOfCreation);
        List<Booking> bookings = bookingRepository
                .findAllByBookerIdAndItem_IdAndStatusAndEndIsBefore(userId, itemId, Status.APPROVED, timeOfCreation);
        if (bookings.isEmpty())
            throw new WrongRequestException("У пользователя с id: " + userId + " отсутствует бронирование для вещи с id: " + item.getId());
        else {
            Comment comment = commentRepository.save(CommentMapper.toComment(commentDto, user, item));
            return CommentMapper.toCommentDto(comment);
        }
    }

    private List<ItemDto> findItemDtoWithNextAndLastBookings(List<Item> itemList) {
        List<Booking> nextBookings = bookingRepository
                .findAllByStartAfterAndStatusAndItemInOrderByStartDesc(LocalDateTime.now(),
                        Status.APPROVED, itemList);
        List<Booking> lastBookings = bookingRepository
                .findAllByEndBeforeAndStatusAndItemInOrderByStartDesc(LocalDateTime.now(),
                        Status.APPROVED, itemList);
        List<ItemDto> itemDtoList = new ArrayList<>();
        itemList.stream().map(ItemMapper::toItemDto).forEach(itemDto -> {
            nextBookings.stream().filter(nextBooking -> Objects.equals(nextBooking.getItem().getId(), itemDto.getId())).forEach(itemDto::setNextBooking);
            lastBookings.stream().filter(lastBooking -> Objects.equals(lastBooking.getItem().getId(), itemDto.getId())).forEach(itemDto::setLastBooking);
            itemDtoList.add(itemDto);
        });
        return itemDtoList;
    }

    private Booking getLastBooking(Long itemId, LocalDateTime currentTime) {
        return bookingRepository
                .findFirstByItemIdAndStatusAndStartIsBeforeOrderByStartDesc(itemId, Status.APPROVED, currentTime);
    }

    private Booking getNextBooking(Long itemId, LocalDateTime currentTime) {
        return bookingRepository
                .findFirstByItemIdAndStatusAndStartIsAfterOrderByStart(itemId, Status.APPROVED, currentTime);
    }
}
