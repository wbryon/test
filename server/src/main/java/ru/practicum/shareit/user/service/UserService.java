package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto update(UserDto userDto);

    UserDto findUserDtoById(Long userId);

    List<UserDto> getAllUsers();

    void delete(Long userId);

}
