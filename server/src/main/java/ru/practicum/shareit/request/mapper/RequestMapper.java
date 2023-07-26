package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;

import java.util.ArrayList;
import java.util.List;

public class RequestMapper {
    public static Request toRequest(RequestDto requestDto) {
        Request request = new Request();
        request.setId(requestDto.getId());
        request.setDescription(requestDto.getDescription());
        request.setRequestorId(requestDto.getRequestorId());
        request.setCreated(requestDto.getCreated());
        return request;
    }

    public static RequestDto toRequestDto(Request request) {
        RequestDto requestDto = new RequestDto();
        requestDto.setId(request.getId());
        requestDto.setDescription(request.getDescription());
        requestDto.setRequestorId(request.getRequestorId());
        requestDto.setCreated(request.getCreated());
        requestDto.setItems(new ArrayList<>());
        return requestDto;
    }

    public static List<RequestDto> toRequestDtoList(List<Request> requests) {
        List<RequestDto> requestDtoList = new ArrayList<>();
        for (Request request : requests) {
            RequestDto requestDto = toRequestDto(request);
            requestDtoList.add(requestDto);
        }
        return requestDtoList;
    }
}
