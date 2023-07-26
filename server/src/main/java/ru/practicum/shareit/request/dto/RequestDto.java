package ru.practicum.shareit.request.dto;

import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RequestDto {
    private Long id;
    @NotNull
    private String description;
    private long requestorId;
    private LocalDateTime created;
    private List<ItemDto> items;
}
