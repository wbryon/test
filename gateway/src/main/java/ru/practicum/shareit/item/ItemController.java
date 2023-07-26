package ru.practicum.shareit.item;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.Marker;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/items")
@Validated
public class ItemController {
    private final ItemClient client;

    public ItemController(ItemClient client) {
        this.client = client;
    }

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody @Valid ItemDto itemDto) {
        return client.create(userId, itemDto);
    }

    @PatchMapping("/{id}")
    @Validated(Marker.OnUpdate.class)
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody ItemDto itemDto,
                                         @PathVariable("id") Long id) {
        return client.update(userId, id, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllOwnerItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer from,
                                                @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return client.getAllOwnerItems(userId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findItemById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable("itemId") Long id) {
        return client.findItemById(userId, id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findItemForRental(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @RequestParam String text,
                                                      @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer from,
                                                      @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return client.findItemForRental(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @PathVariable("itemId") Long itemId,
                                                @RequestBody @Valid CommentDto commentDto) {
        return client.createComment(userId, itemId, commentDto);
    }
}