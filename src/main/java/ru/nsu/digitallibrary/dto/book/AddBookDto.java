package ru.nsu.digitallibrary.dto.book;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AddBookDto {

    @Schema(description = "Название книги")
    private String title;

    @Schema(description = "Автор")
    private String author;

    @Schema(description = "Жанр")
    private String genre;

    @Schema(description = "Описание книги")
    private String description;

    @Schema(description = "ISBN")
    private String isbn;

    @Schema(description = "Оценка книге")
    private Double score;

    @Schema(description = "Количество проголосовавших")
    private Integer votersNumber;

}
