package ru.nsu.digitallibrary.dto.book;

import lombok.Data;

import java.util.List;

@Data
public class BookDto {

    private Long id;

    private String title;

    private String author;

    private String genre;

    private String description;

    private String isbn;

    private List<String> files; //todo прокинуть

}
