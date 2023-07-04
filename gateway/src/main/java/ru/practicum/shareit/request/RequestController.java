package ru.practicum.shareit.request;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.RequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping(path = "/requests")
@Validated
public class RequestController {
    private final RequestClient client;

    public RequestController(RequestClient client) {
        this.client = client;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody @Valid RequestDto requestDto) {
        return client.create(userId, requestDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findByUserIdAndRequestId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable("requestId") Long requestId) {
        return client.findByUserIdAndRequestId(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllRequestsByRequestor(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return client.getAllRequestsByRequestor(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer from,
                                         @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        return client.getAllRequests(userId, from, size);
    }
}
