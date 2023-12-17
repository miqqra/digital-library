package ru.nsu.digitallibrary.entity;

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
import javax.persistence.Lob;

@Getter
@Setter
@Document(indexName = "book_file")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class BookFile {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Field(type = FieldType.Text, name = "name")
    private String name;

    @Field(type = FieldType.Long, name = "book_id")
    private Long bookId;

    @Field(type = FieldType.Text, name = "content_type")
    private String contentType;

    @Field(type = FieldType.Long, name = "size")
    private Long size;

    @Lob
    @Field(type = FieldType.Binary, name = "data")
    private byte[] data;
}
