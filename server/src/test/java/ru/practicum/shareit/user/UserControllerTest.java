package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.WrongRequestException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class)
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    private UserService userServiceMock;

    @SneakyThrows
    @Test
    void getById_UserFound_ReturnUser() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("test");
        user.setEmail("test@mail.com");
        when(userServiceMock.findUserDtoById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", user.getId()))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.name").value((user.getName())));
        verify(userServiceMock).findUserDtoById(user.getId());
    }

    @SneakyThrows
    @Test
    void getById_UserNotFound_returnNotFoundException() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("test");
        user.setEmail("test@mail.com");

        when(userServiceMock.findUserDtoById(1L)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/users/{id}", user.getId())).andExpect(status().isNotFound());
        verify(userServiceMock).findUserDtoById(1L);

    }

    @SneakyThrows
    @Test
    void updateUser_UserFound_ReturnOk() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("test");
        user.setEmail("test@mail.com");
        when(userServiceMock.update(user)).thenReturn(user);

        mockMvc.perform(patch("/users/{id}", user.getId())
                        .content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect((jsonPath("$.email").value(user.getEmail())));
        verify(userServiceMock).update(user);

    }

    @SneakyThrows
    @Test
    void updateUser_UserIsNotValid_returnValidationException() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("test");
        user.setEmail("test@mail.com");
        when(userServiceMock.update(user)).thenThrow(WrongRequestException.class);

        mockMvc.perform(patch("/users/{id}", user.getId())
                        .content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest());

        verify(userServiceMock).update(user);
    }

    @SneakyThrows
    @Test
    void getUsers() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("test");
        user.setEmail("test@mail.com");
        when(userServiceMock.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(user))));
        verify(userServiceMock).getAllUsers();
    }

    @SneakyThrows
    @Test
    void addUser_UserValid_ReturnUser() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("test");
        user.setEmail("test@mail.com");
        when(userServiceMock.create(user)).thenReturn(user);

        mockMvc.perform(post("/users")
                        .content(new ObjectMapper().writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect((jsonPath("$.email").value(user.getEmail())));
        verify(userServiceMock).create(user);
    }

    @SneakyThrows
    @Test
    void create_UserIsInvalid_ReturnValidationException() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("test");
        user.setEmail("test@mail.com");
        when(userServiceMock.create(user)).thenThrow(WrongRequestException.class);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest());
        verify(userServiceMock).create(user);
    }

    @SneakyThrows
    @Test
    void deleteById() {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("test");
        user.setEmail("test@mail.com");
        mockMvc.perform(delete("/users/{id}", user.getId()))
                .andExpect(status().isOk());
        verify(userServiceMock).delete(user.getId());
    }
}