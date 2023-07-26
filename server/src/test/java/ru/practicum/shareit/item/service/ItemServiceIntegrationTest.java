package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.WrongRequestException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ItemServiceIntegrationTest {
    UserDto itemOwner;
    UserDto user;
    ItemDto item;
    UserDto createdOwner;
    UserDto createdUser;
    ItemDto createdItem;
    ItemDto itemForUpdate;
    CommentDto commentDto;
    Booking booking;
    Booking booking2;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void beforeEach() {
        itemOwner = new UserDto();
        itemOwner.setName("testNameOwner");
        itemOwner.setEmail("testowner@mail.com");
        createdOwner = userService.create(itemOwner);

        item = new ItemDto();
        item.setName("testItem");
        item.setDescription("testItemDescription");
        item.setAvailable(true);
        createdItem = itemService.create(createdOwner.getId(), item);

        itemForUpdate = new ItemDto();
        item.setName("updatedItemName");
        item.setDescription("updTestItemDescription");
        item.setAvailable(true);

        user = new UserDto();
        user.setName("testNameUser");
        user.setEmail("testEmailUser@mail.com");
        createdUser = userService.create(user);

        booking = new Booking();
        booking.setBooker(UserMapper.toUser(createdUser));
        booking.setItem(ItemMapper.toItem(createdItem, UserMapper.toUser(createdOwner)));
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(2));
        booking.setStatus(Status.APPROVED);
        booking = bookingRepository.save(booking);

        commentDto = new CommentDto();
        commentDto.setCreated(LocalDateTime.now());
        commentDto.setText("testText");
        commentDto.setItemId(createdItem.getId());
        commentDto.setUserId(createdUser.getId());
        commentDto.setAuthorName(createdUser.getName());
    }

    @Test
    void getById() {
        ItemDto actual = itemService.findItemById(createdUser.getId(), createdItem.getId());
        assertNull(actual.getLastBooking());
        assertNull(actual.getNextBooking());
    }

    @Test
    void getById_isOwner() {
        ItemDto actual = itemService.findItemById(createdOwner.getId(), createdItem.getId());
        assertEquals(actual.getLastBooking().getId(), booking.getId());
    }

    @Test
    void getAllByOwner() {
        List<ItemDto> list = itemService.getAllOwnerItems(createdOwner.getId(), 0, 20);
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    void searchAvailableItem_isCorrect_returnList() {
        List<ItemDto> itemDtoList = itemService.findItemForRental("test");
        assertNotNull(itemDtoList);
        assertEquals(1, itemDtoList.size());
        assertEquals(createdItem.getName(), itemDtoList.get(0).getName());
    }

    @Test
    void searchAvailableItem_isIncorrect_returnList() {
        List<ItemDto> itemDtoList = itemService.findItemForRental("blabla");
        assertNotNull(itemDtoList);
        assertEquals(0, itemDtoList.size());
    }

    @Test
    void update_isValid() {
        ItemDto actual = itemService.update(createdOwner.getId(), createdItem.getId(), itemForUpdate);
        assertEquals(actual.getName(), createdItem.getName());
        assertEquals(actual.getDescription(), createdItem.getDescription());
    }

    @Test
    void update_ownerIsInvalid() {
        assertThrows(NotFoundException.class, () -> itemService.update(999L, createdItem.getId(), createdItem));
    }

    @Test
    void createComment_isValid() {
        CommentDto actual = itemService.createComment(commentDto, createdUser.getId(), createdItem.getId());
        assertEquals(actual.getCreated().toString(), commentDto.getCreated().toString());
        assertEquals(actual.getItemId(), createdItem.getId());
        assertEquals(actual.getText(), commentDto.getText());
        assertEquals(actual.getAuthorName(), commentDto.getAuthorName());
    }

    @Test
    void createComment_isUserHasNoBookings() {
        assertThrows(WrongRequestException.class,
                () -> itemService.createComment(commentDto, createdOwner.getId(), createdItem.getId()));
    }

    @AfterEach
    void afterEach() {
        bookingService.delete(booking.getId());
        commentRepository.deleteAll();
        itemService.delete(createdOwner.getId(), createdItem.getId());
        userService.delete(createdOwner.getId());
        userService.delete(createdUser.getId());
    }
}