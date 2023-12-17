package ru.nsu.digitallibrary.mapper;

import lombok.SneakyThrows;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.nsu.digitallibrary.dto.book.AddBookDto;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.entity.Book;
import ru.nsu.digitallibrary.entity.BookFile;

@Mapper
public abstract class BookMapper {

    public abstract BookDto toDto(Book source);

    public abstract Book toEntity(BookDto source);

    @Mapping(target = "id", ignore = true)
    public abstract Book toEntity(AddBookDto source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contentType", ignore = true)
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "data", ignore = true)
    @Mapping(target = "name", ignore = true)
    public abstract BookFile toBookFile(Long bookId, MultipartFile file);

    @AfterMapping
    @SneakyThrows
    protected void postMap(@MappingTarget BookFile target, Long bookId, MultipartFile file) {
        target
                .setName(StringUtils.cleanPath(file.getOriginalFilename()))
                .setContentType(file.getContentType())
                .setSize(file.getSize())
                .setData(file.getBytes());
    }
}
