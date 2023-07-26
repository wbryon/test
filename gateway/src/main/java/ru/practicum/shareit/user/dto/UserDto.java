package ru.practicum.shareit.user.dto;

import lombok.Data;
import ru.practicum.shareit.validation.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class UserDto {
    @NotNull(groups = Marker.OnUpdate.class)
    private Long id;
    private String name;
    @NotNull(groups = Marker.OnCreate.class)
    @Email(groups = {Marker.OnUpdate.class, Marker.OnCreate.class})
    private String email;
}