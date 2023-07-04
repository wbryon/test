package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserServiceIntegrationTest {

    UserDto userDtoFromController;
    UserDto createdUserFromRepo;
    @Autowired
    private UserService userService;

    @BeforeEach
    void beforeEach() {
        userDtoFromController = new UserDto();
        userDtoFromController.setName("testName");
        userDtoFromController.setEmail("testEmail@mail.com");
        createdUserFromRepo = userService.create(userDtoFromController);
    }

    @Test
    void shouldGetById() {
        UserDto actual = userService.findUserDtoById(createdUserFromRepo.getId());
        assertEquals(actual, createdUserFromRepo);
    }

    @Test
    void shouldGetById_userIdIsInvalid_throwException() {
        assertThrows(NotFoundException.class, () -> userService.findUserDtoById(999L));
    }

    @Test
    void shouldCreate() {
        UserDto userDto = new UserDto();
        userDto.setName("testName2");
        userDto.setEmail("testEmail2@mail.com");
        UserDto created = userService.create(userDto);
        assertEquals(userDto.getName(), created.getName());
        assertEquals(userDto.getEmail(), created.getEmail());
    }

    @Test
    void shouldCreate_isEmailExists() {
        UserDto userDto = new UserDto();
        userDto.setName("testName2");
        userDto.setEmail("testEmail@mail22.com");
        userService.create(userDto);
        assertThrows(DataIntegrityViolationException.class, () -> userService.create(userDto));
    }

    @Test
    void shouldUpdate() {
        userDtoFromController.setName("updatedName");
        userDtoFromController.setEmail("updated@Email.com");
        userDtoFromController.setId(createdUserFromRepo.getId());
        UserDto actual = userService.update(userDtoFromController);
        assertEquals("updatedName", actual.getName());
        assertEquals("updated@Email.com", actual.getEmail());

    }

    @Test
    void shouldNotUpdate_UserIdIsInvalid_throwException() {
        userDtoFromController.setName("updatedName");
        userDtoFromController.setEmail("updated@Email.com");
        userDtoFromController.setId(999L);
        assertThrows(NotFoundException.class, () -> userService.update(userDtoFromController));
    }

    @Test
    void shouldGetAll() {
        List<UserDto> list = userService.getAllUsers();
        assertFalse(list.isEmpty());
    }

    @AfterEach
    void shouldDeleteById() {
        userService.delete(createdUserFromRepo.getId());
    }
}