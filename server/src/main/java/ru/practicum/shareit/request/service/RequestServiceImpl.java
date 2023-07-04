package ru.practicum.shareit.request.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestServiceImpl implements RequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RequestRepository requestRepository;

    public RequestServiceImpl(UserRepository userRepository, ItemRepository itemRepository,
                              RequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public RequestDto create(RequestDto requestDto, Long userId) {
        userRepository.findByUserId(userId);
        requestDto.setRequestorId(userId);
        requestDto.setCreated(LocalDateTime.now());
        Request request = requestRepository.save(RequestMapper.toRequest(requestDto));
        return RequestMapper.toRequestDto(request);
    }

    @Override
    public RequestDto findByUserIdAndRequestId(Long userId, Long requestId) {
        userRepository.findByUserId(userId);
        Request request = requestRepository.findByRequestId(requestId);
        List<Item> items = itemRepository.findItemByRequest(request);
        RequestDto requestDto = RequestMapper.toRequestDto(request);
        requestDto.setItems(ItemMapper.toItemDtolist(items));
        return requestDto;
    }

    @Override
    public List<RequestDto> getAllRequestsByRequestor(long userId) {
        userRepository.findByUserId(userId);
        List<Request> requests = requestRepository.findAllByRequestorId(userId);
        return getRequestDtoWithItems(requests);
    }

    @Override
    public List<RequestDto> getAllRequests(long userId, int from, int size) {
        userRepository.findByUserId(userId);
        List<Request> requests = requestRepository.findRequestsByRequestorIdNotOrderByCreatedDesc(userId, PageRequest.of(from, size));
        return getRequestDtoWithItems(requests);
    }

    @Override
    public void delete(Long userId, Long requestId) {
        userRepository.findByUserId(userId);
        requestRepository.deleteById(requestId);
    }

    private List<RequestDto> getRequestDtoWithItems(List<Request> requests) {
        List<RequestDto> requestDtoList = RequestMapper.toRequestDtoList(requests);
        List<Item> itemsWithRequestsId = itemRepository.findItemByRequestIn(requests);
        for (RequestDto request : requestDtoList) {
            for (Item item : itemsWithRequestsId) {
                if (request.getId().equals(item.getRequest().getId()))
                    request.getItems().add(ItemMapper.toItemDto(item));
            }
        }
        return requestDtoList;
    }
}
