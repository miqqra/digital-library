package ru.nsu.digitallibrary.service;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.nsu.digitallibrary.dto.book.AddBookDto;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.dto.book.BookIdsDto;
import ru.nsu.digitallibrary.dto.book.BookTextDto;
import ru.nsu.digitallibrary.dto.search.SearchFacetDto;
import ru.nsu.digitallibrary.entity.elasticsearch.BookData;
import ru.nsu.digitallibrary.entity.postgres.Book;
import ru.nsu.digitallibrary.exception.ClientException;
import ru.nsu.digitallibrary.mapper.BookMapper;
import ru.nsu.digitallibrary.model.ElasticsearchFindStrategy;
import ru.nsu.digitallibrary.model.Fb2Book;
import ru.nsu.digitallibrary.repository.postgres.BookRepository;
import ru.nsu.digitallibrary.service.data.ElasticsearchDataService;

@Service
@RequiredArgsConstructor
public class BookService {

    private final HttpServletRequest request;

    private final ElasticsearchDataService elasticsearchDataService;

    private final BookRepository bookRepository;

    private final BookMapper mapper;

    @Value("${app.scripts.file.path}")
    private String FILE_PATH;

    @Value("${app.scripts.file.python}")
    private String PYTHON_EXE;

    @Value("${app.scripts.find}")
    private String PYTHON_FIND_BOOK_SCRIPT_PATH;

    @Value("${app.scripts.add}")
    private String PYTHON_ADD_BOOK_SCRIPT_PATH;

    @Transactional(readOnly = true)
    public List<BookDto> searchBook(List<SearchFacetDto> facets) {
        List<BookDto> booksFromNeuro = Optional.of(facets)
                .stream()
                .flatMap(Collection::stream)
                .filter(v -> v.getStrategy().equals(ElasticsearchFindStrategy.DATA))
                .map(facet -> findBookIdsNeuro(facet.getSearchText()))
                .flatMap(Collection::stream)
                .map(this::getBookData)
                .collect(Collectors.toList());

        List<BookDto> booksFromElastic = Optional.of(facets)
                .map(elasticsearchDataService::findBooks)
                .orElse(null);

        return mergeResults(booksFromNeuro, booksFromElastic);
    }

    @Transactional(readOnly = true)
    public BookIdsDto getBookIds() {
        return new BookIdsDto().setIds(elasticsearchDataService.findAllBookIds());
    }

    @Transactional(readOnly = true)
    public BookTextDto getBookText(String id) {
        String text = Optional.of(id)
                .map(elasticsearchDataService::findById)
                .map(BookData::getData)
                .orElseThrow(() -> ClientException.of(HttpStatus.NOT_FOUND, "Не удалось найти книгу"));

        return new BookTextDto()
                .setId(id)
                .setText(text);
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
                .map(elasticsearchDataService::findShortenedById)
                .orElseThrow(() -> ClientException.of(HttpStatus.NOT_FOUND, "Книга не найдена"));
    }

    @Transactional
    public BookDto updateBook(BookDto bookDto) {
        BookData book = Optional.of(bookDto)
                .map(BookDto::getId)
                .map(elasticsearchDataService::findById)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Такой книги не существует"));

        return Optional.of(bookDto)
                .map(v -> mapper.toEntity(book, v))
                .map(elasticsearchDataService::save)
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

        Fb2Book fb2Book = new Fb2Book(file, request);

        BookData elasticBook = Optional.of(fb2Book)
                .map(mapper::toEntity)
                .map(book -> Optional.of(book)
                        .map(v -> elasticsearchDataService.findByAuthorAndTitle(v.getAuthor(), v.getTitle()))
                        .orElseGet(() -> elasticsearchDataService.save(book))
                )
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось сохранить книгу"));

        Optional.of(elasticBook)
                .map(bookData -> addBookNeuro(bookData))
                .filter(v -> !v)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, """
                        Не удалось добавить книгу в нейросеть, поиск может работать неточно. Попробуйте добавить книгу еще раз.
                        """));

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
                .map(elasticsearchDataService::save)
                .map(mapper::toDto)
                .orElseThrow(() -> ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось сохранить книгу"));
    }

    @Transactional
    public void uploadFile(String bookId, MultipartFile file) {
        BookData book = Optional.of(bookId)
                .map(elasticsearchDataService::findById)
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
                    .map(elasticsearchDataService::save);
        } catch (IOException e) {
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось загрузить новый файл для книги");
        }
    }

    @Transactional
    public void deleteBook(String id) {
        Optional.of(id)
                .map(elasticsearchDataService::findShortenedById)
                .ifPresentOrElse(
                        book -> {
                            elasticsearchDataService.delete(book.getId());
                            bookRepository.deleteBookByElasticId(id);
                        },
                        () -> {
                            throw ClientException.of(HttpStatus.BAD_REQUEST, "Такой книги не существует");
                        });
    }

    @Transactional
    public void rateBook(String id, Double score) {
        BookData bookData = Optional.of(id)
                .map(elasticsearchDataService::findById)
                .orElseThrow(() -> ClientException.of(HttpStatus.NOT_FOUND, "Книга не найдена"));

        Optional.of(bookData)
                .map(v -> {
                            Double oldScore = Optional.of(v).map(BookData::getScore).orElse(0.0);
                            Integer oldVotersNumber = Optional.of(v).map(BookData::getVotersNumber).orElse(0);
                            Double newScore = (oldScore * oldVotersNumber + score) / (oldVotersNumber + 1);
                            return v.setVotersNumber(oldVotersNumber + 1)
                                    .setScore(newScore);
                        }
                )
                .map(elasticsearchDataService::save);
    }

    public void deleteBookFile(String id) {
        bookRepository.deleteBookByElasticId(id);
    }

    private List<BookDto> mergeResults(List<BookDto> booksFromNeuro, List<BookDto> booksFromElastic) {
        Set<String> neuroResultsSet = booksFromNeuro.stream().map(BookDto::getId).collect(Collectors.toSet());

        booksFromElastic.stream()
                .filter(book -> !neuroResultsSet.contains(book.getId()))
                .forEach(booksFromNeuro::add);

        return booksFromNeuro;
    }

    private String parseBookForElastic(MultipartFile multipartFile) {
        try {
            return new String(multipartFile.getBytes());
        } catch (Exception e) {
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось сохранить файл книги");
        }
    }

    private List<String> findBookIdsNeuro(String searchQuery) {
        try {

            ProcessBuilder processBuilder = new ProcessBuilder(
                    PYTHON_EXE,
                    PYTHON_FIND_BOOK_SCRIPT_PATH,
                    searchQuery);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            return Optional.of(output)
                    .map(v -> v.split(" "))
                    .map(v -> Arrays.stream(v).toList())
                    .orElse(Collections.emptyList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private boolean addBookNeuro(BookData bookData) {
        try (PrintWriter out = new PrintWriter(FILE_PATH)) {
            out.println(bookData.getData());

            ProcessBuilder processBuilder = new ProcessBuilder(
                    PYTHON_EXE,
                    PYTHON_ADD_BOOK_SCRIPT_PATH,
                    bookData.getId(),
                    FILE_PATH);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            return Boolean.parseBoolean(output);
        } catch (IOException e) {
            return false;
        }
    }
}
