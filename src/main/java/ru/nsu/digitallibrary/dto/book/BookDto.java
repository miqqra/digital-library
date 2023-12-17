package ru.nsu.digitallibrary.dto.book;

import lombok.Data;

@Data
public class BookDto {

    private Long id;

    private String title;

    private String author;

    private String genre;

    private String description;

    private String isbn;

}
