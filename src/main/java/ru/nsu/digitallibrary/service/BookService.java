package ru.nsu.digitallibrary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.nsu.digitallibrary.dto.book.AddBookDto;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.entity.postgres.Book;
import ru.nsu.digitallibrary.exception.ClientException;
import ru.nsu.digitallibrary.mapper.BookMapper;
import ru.nsu.digitallibrary.repository.elasticsearch.BookDataElasticSearchRepository;
import ru.nsu.digitallibrary.repository.postgres.BookRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final ElasticsearchBookService elasticsearchBookService;

    private final BookRepository bookRepository;
    private final BookDataElasticSearchRepository bookDataElasticSearchRepository;

    private final BookMapper mapper;

    @Transactional(readOnly = true)
    public List<BookDto> searchBook(String searchQuery) {
        return Optional.of(searchQuery)
                .map(elasticsearchBookService::searchBook)
                .stream()
                .flatMap(Collection::stream)
                .map(bookRepository::findBookById)
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] downloadBook(Long id) {
        return Optional.of(id)
                .flatMap(bookRepository::findById)
                .map(Book::getFile)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось скачать книгу"));
    }

    @Transactional(readOnly = true)
    public BookDto getBookData(Long id) {
        return Optional.of(id)
                .map(bookRepository::findBookById)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.NOT_FOUND, "Книга не найдена"));
    }

    @Transactional(readOnly = true)
    public BookDto getBookData(String isbn) {
        return Optional.of(isbn)
                .map(bookRepository::findBookByIsbn)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.NOT_FOUND, "Книга не найдена"));
    }

    @Transactional
    public BookDto updateBook(BookDto bookDto) {
        Book book = Optional.of(bookDto)
                .map(BookDto::getId)
                .map(bookRepository::findBookById)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Такой книги не существует"));

        return Optional.of(bookDto)
                .map(mapper::toEntity)
                .map(newBook -> newBook.setFile(book.getFile()))
                .map(bookRepository::save)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось обновить данные о книге"));
    }

    @Transactional
    public BookDto addBook(AddBookDto bookDto) {
        return Optional.of(bookDto)
                .map(mapper::toEntity)
                .map(bookRepository::save)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось сохранить книгу"));
    }

    @Transactional
    public void uploadFile(Long bookId, MultipartFile file) {
        Optional.of(bookId)
                .map(bookDataElasticSearchRepository::findBookDataByBookId)
                .ifPresent(bookDataElasticSearchRepository::delete);

        try {
            if (file == null)
                throw ClientException.of(HttpStatus.BAD_REQUEST, "Нет файла");

            bookRepository.updateBookFile(file.getBytes(), file.getOriginalFilename(), bookId);
            Optional.of(file)
                    .map(this::parseBookForElastic)
                    .map(data -> mapper.toBookData(bookId, data))
                    .map(bookDataElasticSearchRepository::save);
        } catch (IOException e) {
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось загрузить новый файл для книги");
        }
    }

    @Transactional
    public void deleteBook(Long id) {
        Optional.of(id)
                .map(bookRepository::findBookById)
                .ifPresentOrElse(
                        bookRepository::delete,
                        () -> {
                            throw ClientException.of(HttpStatus.BAD_REQUEST, "Такой книги не существует");
                        });
        deleteBookFile(id);
    }

    @Transactional
    public void deleteBookFile(Long id) {
        bookRepository.updateBookFile(null, null, id);
    }

    private String parseBookForElastic(MultipartFile multipartFile) {
        try {
            return new String(multipartFile.getBytes());
        } catch (Exception e) {
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось сохранить файл книги");
        }
    }
}
