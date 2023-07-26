package ru.practicum.shareit.request.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class RequestDto {
    private Long id;
    @NotNull
    private String description;
    @NotNull
    private long requestorId;
    private LocalDateTime created;
}
