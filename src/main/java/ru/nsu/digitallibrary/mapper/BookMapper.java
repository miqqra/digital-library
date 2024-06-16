package ru.nsu.digitallibrary.mapper;

import java.util.List;
import java.util.Optional;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.nsu.digitallibrary.dto.book.AddBookDto;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.entity.elasticsearch.BookData;
import ru.nsu.digitallibrary.entity.elasticsearch.BookShortenedData;
import ru.nsu.digitallibrary.entity.postgres.Book;
import ru.nsu.digitallibrary.model.Fb2Book;

@Mapper(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class BookMapper {

    @AfterMapping
    protected void postMap(@MappingTarget BookDto target, Book source) {
        Optional.of(source)
                .map(Book::getFileName)
                .map(List::of)
                .ifPresent(target::setFiles);
    }

    @Mapping(target = "files", ignore = true)
    public abstract BookDto toDto(BookData source);

    @Mapping(target = "files", ignore = true)
    public abstract BookDto toDto(BookShortenedData source);

    @Mapping(target = "data", ignore = true)
    public abstract BookData toEntity(BookDto source);

    public abstract BookData toEntity(@MappingTarget BookData target, BookDto source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "data", ignore = true)
    @Mapping(target = "score", source = "score", defaultValue = "0")
    @Mapping(target = "votersNumber", source = "votersNumber", defaultValue = "0")
    public abstract BookData toEntity(AddBookDto source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", expression = "java(source.getTitle())")
    @Mapping(target = "author", expression = "java(source.getAuthors())")
    @Mapping(target = "genre", expression = "java(source.getGenres())")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "isbn", ignore = true)
    @Mapping(target = "data", expression = "java(source.getData())")
    @Mapping(target = "score", constant = "0")
    @Mapping(target = "votersNumber", constant = "0")
    public abstract BookData toEntity(Fb2Book source);

    @AfterMapping
    protected void postMap(@MappingTarget Book target, AddBookDto source) {
        target.setFile(new byte[]{});
    }

    @Mapping(target = "data", source = "data")
    public abstract BookData toBookData(BookData bookData, String data);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "elasticId", source = "source.id")
    public abstract Book toPgBook(BookData source, byte[] file, String fileName);
}
