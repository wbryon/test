package ru.practicum.shareit.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Data
public class UserDto {
    private Long id;
    private String name;
    @Email(message = "Невалидная электронная почта")
    @NotNull
    private String email;

    public UserDto(Long id) {
        this.id = id;
    }

    public UserDto(long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
