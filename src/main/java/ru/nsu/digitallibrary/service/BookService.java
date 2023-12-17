package ru.nsu.digitallibrary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.nsu.digitallibrary.dto.book.AddBookDto;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.entity.BookFile;
import ru.nsu.digitallibrary.exception.ClientException;
import ru.nsu.digitallibrary.mapper.BookMapper;
import ru.nsu.digitallibrary.repository.elasticsearch.BookElasticSearchRepository;
import ru.nsu.digitallibrary.repository.elasticsearch.BookFileElasticSearchRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookElasticSearchRepository bookRepository;
    private final BookFileElasticSearchRepository bookFileRepository;

    private final BookMapper mapper;

    public List<BookDto> searchBook(String searchQuery) {
        return null;
        //todo
    }

    public byte[] downloadBook(Long id) {
        return Optional.of(id)
                .flatMap(bookFileRepository::findById)
                .map(BookFile::getData)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось скачать книгу"));

    }

    public BookDto getBookData(Long id) {
        return Optional.of(id)
                .map(bookRepository::findBookById)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.NOT_FOUND, "Книга не найдена"));
    }

    public BookDto getBookData(String isbn) {
        return Optional.of(isbn)
                .map(bookRepository::findBookByIsbn)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.NOT_FOUND, "Книга не найдена"));
    }

    public BookDto updateBook(BookDto bookDto) {
        if (!bookRepository.existsBookById(bookDto.getId())) {
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Такой книги не существует");
        }

        return Optional.of(bookDto)
                .map(mapper::toEntity)
                .map(bookRepository::save)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось обновить данные о книге"));
    }

    public BookDto addBook(AddBookDto bookDto) {
        return Optional.of(bookDto)
                .map(mapper::toEntity)
                .map(bookRepository::save)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось сохранить книгу"));
    }

    public void uploadFile(Long bookId, MultipartFile file) {
        if (bookFileRepository.existsBookFileByBookId(bookId)) {
            bookFileRepository.deleteBookFileByBookId(bookId);
        }
        Optional.of(file)
                .map(f -> mapper.toBookFile(bookId, f))
                .map(bookFileRepository::save)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось загрузить новый файл для книги"));
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
        deleteBookFile(id);
    }

    public void deleteBookFile(Long id) {
        bookFileRepository.deleteById(id);
    }
}
