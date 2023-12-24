package ru.nsu.digitallibrary.entity.elasticsearch;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@Document(indexName = "lib_book_file")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class BookData {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Field(type = FieldType.Long, name = "book_id")
    private Long bookId;

    @Field(type = FieldType.Text, name = "data")
    private String data;
}
