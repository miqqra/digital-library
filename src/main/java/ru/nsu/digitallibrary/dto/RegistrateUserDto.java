package ru.nsu.digitallibrary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrateUserDto {

    @NotBlank
    @Schema(description = "Ник пользователя")
    private String nickname;

    @NotBlank
    @Schema(description = "Пароль")
    private String password;
}

