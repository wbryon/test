package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase
class UserRepositoryTest {
    User user1;
    User createdUser1;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void beforeEach() {
        user1 = new User();
        user1.setName("user1");
        user1.setEmail("user1@mail.com");
        createdUser1 = userRepository.save(user1);
    }

    @Test
    void shouldFindById() {
        assertEquals(user1.getName(), userRepository.findByUserId(createdUser1.getId()).getName());
    }

    @Test
    void shouldNotFindById() {
        assertThrows(NotFoundException.class, () -> userRepository.findByUserId(999L));
    }
}