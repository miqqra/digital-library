package ru.nsu.digitallibrary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginUserDto {

    @NotBlank
    @Schema(description = "Логин(ник пользователя)")
    private String login;

    @NotBlank
    @Schema(description = "Пароль")
    private String password;
}

