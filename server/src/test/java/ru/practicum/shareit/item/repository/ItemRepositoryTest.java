package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase
class ItemRepositoryTest {
    User createdUser1;
    User createdUser2;
    Item createdItem1;
    Item createdItem2;
    Request createdRequest1;
    Request createdRequest2;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RequestRepository requestRepository;

    @BeforeEach
    public void beforeEach() {
        User user1 = new User();
        user1.setName("user1");
        user1.setEmail("user1@mail.com");
        createdUser1 = userRepository.save(user1);

        User user2 = new User();
        user2.setName("user2");
        user2.setEmail("user2@mail.com");
        createdUser2 = userRepository.save(user2);
        Request request1 = new Request();
        request1.setDescription("testDescription1");
        request1.setRequestorId(user1.getId());
        request1.setCreated(LocalDateTime.now().minusDays(5));
        createdRequest1 = requestRepository.save(request1);

        Request request2 = new Request();
        request2.setDescription("testDescription1");
        request2.setRequestorId(user1.getId());
        request2.setCreated(LocalDateTime.now().minusDays(2));
        createdRequest2 = requestRepository.save(request2);

        Item item1 = new Item();
        item1.setName("testItem");
        item1.setDescription("testItemDescription");
        item1.setAvailable(true);
        item1.setOwner(createdUser1);
        item1.setRequest(createdRequest1);
        createdItem1 = itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("testItem2");
        item2.setDescription("testItem2Description");
        item2.setAvailable(true);
        item2.setOwner(createdUser2);
        item2.setRequest(createdRequest2);
        createdItem2 = itemRepository.save(item2);
    }

    @Test
    void getItemsByQuery_isValid() {
        assertEquals(List.of(createdItem2), itemRepository.findItemForRental("testItem2"));
    }

    @Test
    void getItemsByQuery_isInvalid() {
        assertEquals(List.of(), itemRepository.findItemForRental("asdasdasgas"));
    }

    @Test
    void getByIdAndCheck_IsItemValid() {
        assertEquals(createdItem1, itemRepository.findByItemId(createdItem1.getId()));
    }

    @Test
    void getByIdAndCheck_IsItemInvalid() {
        assertThrows(NotFoundException.class, () -> itemRepository.findByItemId(999L));
    }

    @Test
    void getItemsByRequestIn() {
        assertEquals(List.of(createdItem1), itemRepository.findItemByRequestIn(List.of(createdRequest1)));
    }

    @Test
    void getItemsByRequest() {
        assertEquals(List.of(createdItem2), itemRepository.findItemByRequest(createdRequest2));
    }
}