package ru.nsu.digitallibrary.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.nsu.digitallibrary.dto.book.AddBookDto;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.entity.elasticsearch.BookData;
import ru.nsu.digitallibrary.entity.postgres.Book;

@Mapper
public abstract class BookMapper {

    public abstract BookDto toDto(Book source);

    @Mapping(target = "file", ignore = true)
    public abstract Book toEntity(BookDto source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "file", ignore = true)
    public abstract Book toEntity(AddBookDto source);

    @Mapping(target = "id", ignore = true)
    public abstract BookData toBookData(Long bookId, String data);
}
