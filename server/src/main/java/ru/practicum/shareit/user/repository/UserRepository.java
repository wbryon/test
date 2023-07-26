package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.model.User;


public interface UserRepository extends JpaRepository<User, Long> {
    default User findByUserId(Long userId) {
        return findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }
}