package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Component
public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    List<ItemDto> getAllOwnerItems(Long userId, Integer from, Integer size);

    ItemDto findItemById(Long userId, Long itemId);

    List<ItemDto> findItemForRental(String text);

    void delete(Long userId, Long itemId);

    CommentDto createComment(CommentDto commentDto, Long userId, Long itemId);
}
