package ru.nsu.digitallibrary.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.nsu.digitallibrary.dto.book.AddBookDto;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.dto.search.SearchFacetDto;
import ru.nsu.digitallibrary.entity.elasticsearch.BookData;
import ru.nsu.digitallibrary.entity.postgres.Book;
import ru.nsu.digitallibrary.exception.ClientException;
import ru.nsu.digitallibrary.mapper.BookMapper;
import ru.nsu.digitallibrary.model.Fb2Book;
import ru.nsu.digitallibrary.repository.elasticsearch.BookDataElasticSearchRepository;
import ru.nsu.digitallibrary.repository.postgres.BookRepository;
import ru.nsu.digitallibrary.service.data.ElasticsearchDataService;

@Service
@RequiredArgsConstructor
public class BookService {

    private final ElasticsearchDataService elasticsearchDataService;

    private final BookRepository bookRepository;

    private final BookDataElasticSearchRepository bookDataElasticSearchRepository;

    private final BookMapper mapper;

    @Transactional(readOnly = true)
    public List<BookDto> searchBook(List<SearchFacetDto> facets) {
        return Optional.of(facets)
                .map(elasticsearchDataService::findBooks)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public byte[] downloadBook(String id) {
        return Optional.of(id)
                .map(bookRepository::findBookByElasticId)
                .map(Book::getFile)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось скачать книгу"));
    }

    @Transactional(readOnly = true)
    public BookDto getBookData(String id) {
        return Optional.of(id)
                .flatMap(bookDataElasticSearchRepository::findById)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.NOT_FOUND, "Книга не найдена"));
    }

    @Transactional
    public BookDto updateBook(BookDto bookDto) {
        BookData book = Optional.of(bookDto)
                .map(BookDto::getId)
                .flatMap(bookDataElasticSearchRepository::findById)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Такой книги не существует"));

        return Optional.of(bookDto)
                .map(mapper::toEntity)
                .map(bookDataElasticSearchRepository::save)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось обновить данные о книге"));
    }

    @Transactional
    public BookDto addBook(MultipartFile file) {
        if (file == null)
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Нет файла");

        byte[] bytes;
        String fileName;
        try {
            bytes = file.getBytes();
            fileName = file.getOriginalFilename();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Fb2Book fb2Book = new Fb2Book(file);

        BookData elasticBook = Optional.of(fb2Book)
                .map(mapper::toEntity)
                .map(book -> Optional.of(book)
                        .map(v -> bookDataElasticSearchRepository.findByAuthorAndTitle(v.getAuthor(), v.getTitle()))
                        .orElseGet(() -> bookDataElasticSearchRepository.save(book))
                )
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось сохранить книгу"));

        Optional.of(elasticBook)
                .map(v -> mapper.toPgBook(v, bytes, fileName))
                .filter(v -> !bookRepository.existsByElasticId(v.getElasticId()))
                .ifPresentOrElse(bookRepository::save,
                        () -> {
                            throw ClientException.of(HttpStatus.BAD_REQUEST, "Книга уже существует");
                        });

        return Optional.of(elasticBook)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось сохранить книгу"));
    }

    @Transactional
    public BookDto addBook(AddBookDto bookDto) {
        return Optional.of(bookDto)
                .map(mapper::toEntity)
                .map(bookDataElasticSearchRepository::save)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось сохранить книгу"));
    }

    @Transactional
    public void uploadFile(String bookId, MultipartFile file) {
        BookData book = Optional.of(bookId)
                .flatMap(bookDataElasticSearchRepository::findById)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Такой книги не существует"));

        Optional.of(bookId)
                .map(bookRepository::findBookByElasticId)
                .ifPresent(bookRepository::delete);

        try {
            if (file == null)
                throw ClientException.of(HttpStatus.BAD_REQUEST, "Нет файла");

            bookRepository.updateBookFile(file.getBytes(), file.getOriginalFilename(), bookId);
            Optional.of(file)
                    .map(this::parseBookForElastic)
                    .map(data -> mapper.toBookData(book, data))
                    .map(bookDataElasticSearchRepository::save);
        } catch (IOException e) {
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось загрузить новый файл для книги");
        }
    }

    @Transactional
    public void deleteBook(String id) {
        Optional.of(id)
                .flatMap(bookDataElasticSearchRepository::findById)
                .ifPresentOrElse(
                        book -> {
                            bookDataElasticSearchRepository.delete(book);
                            bookRepository.deleteBookByElasticId(id);
                        },
                        () -> {
                            throw ClientException.of(HttpStatus.BAD_REQUEST, "Такой книги не существует");
                        });
    }

    @Transactional
    public void rateBook(String id, Double score) {
        BookData bookData = Optional.of(id)
                .flatMap(bookDataElasticSearchRepository::findById)
                .orElseThrow(() -> ClientException.of(HttpStatus.NOT_FOUND, "Книга не найдена"));

        Optional.of(bookData)
                .map(v -> {
                            Double newScore = (v.getScore() * v.getVotersNumber() + score) / (v.getVotersNumber() + 1);
                            return v.setVotersNumber(v.getVotersNumber() + 1)
                                    .setScore(newScore);
                        }
                )
                .map(bookDataElasticSearchRepository::save);
    }

    public void deleteBookFile(String id) {
        bookRepository.deleteBookByElasticId(id);
    }

    private String parseBookForElastic(MultipartFile multipartFile) {
        try {
            return new String(multipartFile.getBytes());
        } catch (Exception e) {
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось сохранить файл книги");
        }
    }
}
