package ru.nsu.digitallibrary.entity.elasticsearch;


import lombok.Data;

@Data
public class BookShortenedData {

    private String id;

    private String title;

    private String author;

    private String genre;

    private String description;

    private String isbn;

    private Double score;

    private Integer votersNumber;
}
