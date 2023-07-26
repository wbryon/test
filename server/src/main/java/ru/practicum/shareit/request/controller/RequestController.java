package ru.practicum.shareit.request.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.service.RequestService;

import java.util.List;


@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public RequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @RequestBody RequestDto requestDto) {
        return requestService.create(requestDto, userId);
    }

    @GetMapping("/{requestId}")
    public RequestDto findByUserIdAndRequestId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @PathVariable Long requestId) {
        return requestService.findByUserIdAndRequestId(userId, requestId);
    }

    @GetMapping
    public List<RequestDto> getAllRequestsByRequestor(@RequestHeader("X-Sharer-User-Id") long userId) {
        return requestService.getAllRequestsByRequestor(userId);
    }

    @GetMapping("/all")
    public List<RequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestParam Integer from,
                                         @RequestParam Integer size) {
        return requestService.getAllRequests(userId, from, size);
    }

    @DeleteMapping("/{requestId}")
    public void delete(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable Long requestId) {
        requestService.delete(userId, requestId);
    }

}
