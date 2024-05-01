package ru.nsu.digitallibrary.mapper;

import java.util.List;
import java.util.Optional;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.nsu.digitallibrary.dto.book.AddBookDto;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.entity.elasticsearch.BookData;
import ru.nsu.digitallibrary.entity.postgres.Book;

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

    @Mapping(target = "files", ignore = true)
    public abstract BookDto toDto(BookData source);

    @Mapping(target = "data", ignore = true)
    public abstract BookData toEntity(BookDto source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "data", ignore = true)
    @Mapping(target = "score", source = "score", defaultValue = "0")
    @Mapping(target = "votersNumber", source = "votersNumber", defaultValue = "0")
    public abstract BookData toEntity(AddBookDto source);

    @AfterMapping
    protected void postMap(@MappingTarget Book target, AddBookDto source) {
        target.setFile(new byte[]{});
    }

    @Mapping(target = "data", source = "data")
    public abstract BookData toBookData(BookData bookData, String data);
}
