package ru.nsu.digitallibrary.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.nsu.digitallibrary.dto.book.AddBookDto;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.entity.elasticsearch.BookData;
import ru.nsu.digitallibrary.entity.postgres.Book;

import java.util.List;
import java.util.Optional;

@Mapper
public abstract class BookMapper {

    @Mapping(target = "files", ignore = true)
    public abstract BookDto toDto(Book source);

    @AfterMapping
    protected void postMap(@MappingTarget BookDto target, Book source) {
        Optional.of(source)
                .map(Book::getFileName)
                .map(List::of)
                .ifPresent(target::setFiles);
    }

    @Mapping(target = "file", ignore = true)
    @Mapping(target = "fileName", ignore = true)
    public abstract Book toEntity(BookDto source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "file", ignore = true)
    @Mapping(target = "fileName", ignore = true)
    public abstract Book toEntity(AddBookDto source);

    @AfterMapping
    protected void postMap(@MappingTarget Book target, AddBookDto source) {
        target.setFile(new byte[]{});
    }

    @Mapping(target = "id", ignore = true)
    public abstract BookData toBookData(Long bookId, String data);
}
