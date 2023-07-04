package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto create(UserDto userDto) {
        User created = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.save(created));
    }

    @Override
    public UserDto update(UserDto userDto) {
        User updatedUser = UserMapper.toUser(userDto);
        User userFromDb = userRepository.findByUserId(updatedUser.getId());
        if (updatedUser.getName() == null)
            updatedUser.setName(userFromDb.getName());
        if (updatedUser.getEmail() == null)
            updatedUser.setEmail(userFromDb.getEmail());
        return UserMapper.toUserDto(userRepository.save(updatedUser));
    }

    @Override
    public UserDto findUserDtoById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<UserDto> list = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            UserDto userDto = UserMapper.toUserDto(user);
            list.add(userDto);
        }
        return list;
    }

    @Override
    public void delete(Long userId) {
        User user = userRepository.getReferenceById(userId);
        userRepository.delete(user);
    }
}
