package ru.nsu.digitallibrary.entity.elasticsearch;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;

@Data
@Document(indexName = "lib_book_file")
public class BookData {

    @Id
    private String id;

    @Field(type = FieldType.Long, name = "book_id")
    private Long bookId;

    @Field(type = FieldType.Text, name = "data")
    private String data;
}
