package ru.practicum.shareit.item.repository;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerIdOrderById(Long owner, PageRequest of);

    @Query("select i from Item i " +
            "where lower(i.name) like lower(concat('%', :search, '%')) " +
            "or lower(i.description) like lower(concat('%', :search, '%')) " +
            "and i.available = true")
    List<Item> findItemForRental(@Param("search") String text);

    List<Item> findItemByRequestIn(List<Request> requestList);

    List<Item> findItemByRequest(Request request);

    default Item findByItemId(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Вещь с id = " + id + " не найдена"));
    }
}