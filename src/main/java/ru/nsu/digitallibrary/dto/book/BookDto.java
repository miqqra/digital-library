package ru.nsu.digitallibrary.dto.book;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class BookDto {

    @Schema(description = "Id книги")
    private String id; //elastic id

    @Schema(description = "Название")
    private String title;

    @Schema(description = "Автор")
    private String author;

    @Schema(description = "Жанр")
    private String genre;

    @Schema(description = "Описание книги")
    private String description;

    @Schema(description = "ISBN")
    private String isbn;

    @Schema(description = "Название файлов книги")
    private List<String> files;

    @Schema(description = "Оценка книге")
    private Double score;

    @Schema(description = "Количество проголосовавших")
    private Integer votersNumber;

}
