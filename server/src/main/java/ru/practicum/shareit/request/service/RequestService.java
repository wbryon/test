package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.RequestDto;

import java.util.List;

public interface RequestService {
    RequestDto findByUserIdAndRequestId(Long userId, Long requestId);

    RequestDto create(RequestDto requestDto, Long userId);

    List<RequestDto> getAllRequestsByRequestor(long userId);

    List<RequestDto> getAllRequests(long userId, int from, int size);

    void delete(Long userId, Long requestId);
}
