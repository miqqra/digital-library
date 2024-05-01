package ru.nsu.digitallibrary.entity.elasticsearch;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "lib_book_file")
public class BookData {

    @Id
    private String id;

    @Field(type = FieldType.Text, name = "title")
    private String title;

    @Field(type = FieldType.Text, name = "author")
    private String author;

    @Field(type = FieldType.Text, name = "genre")
    private String genre;

    @Field(type = FieldType.Text, name = "description")
    private String description;

    @Field(type = FieldType.Keyword, name = "isbn")
    private String isbn;

    @Field(type = FieldType.Text, name = "data")
    private String data;

    @Field(type = FieldType.Double, name = "score")
    private Double score;

    @Field(type = FieldType.Integer, name = "votersNumber")
    private Integer votersNumber;
}
