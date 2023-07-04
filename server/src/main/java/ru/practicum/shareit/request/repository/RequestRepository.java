package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    default Request findByRequestId(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Запрос с id = " + id + " не найден"));
    }

    List<Request> findAllByRequestorId(Long userId);

    List<Request> findRequestsByRequestorIdNotOrderByCreatedDesc(Long userId, Pageable pageable);
}
