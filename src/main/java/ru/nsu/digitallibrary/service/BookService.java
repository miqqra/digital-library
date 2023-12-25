package ru.nsu.digitallibrary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.nsu.digitallibrary.dto.book.AddBookDto;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.entity.postgres.Book;
import ru.nsu.digitallibrary.exception.ClientException;
import ru.nsu.digitallibrary.mapper.BookMapper;
import ru.nsu.digitallibrary.repository.elasticsearch.BookDataElasticSearchRepository;
import ru.nsu.digitallibrary.repository.elasticsearch.BookRepository;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookDataElasticSearchRepository bookDataElasticSearchRepository;

    private final BookMapper mapper;

    @Transactional(readOnly = true)
    public List<BookDto> searchBook(String searchQuery) {
        try {
            String pythonScriptPath = "C:\\Users\\User\\IdeaProjects\\digital-library\\src\\main\\resources\\script.py";

            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath, searchQuery);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> output = List.of(reader.readLine().split(" "));
            //todo elastic

        } catch (IOException e) {
            throw ClientException.of(HttpStatus.NOT_FOUND, "Не удалось произвести поиск");
        }

        return null;
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
        if (bookDataElasticSearchRepository.existsBookDataByBookId(bookId)) {
            bookDataElasticSearchRepository.deleteBookDataByBookId(bookId);
        }

        try {
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
        bookRepository.deleteById(id);
        deleteBookFile(id);
    }

    public void deleteBookFile(Long id) {
        bookRepository.updateBookFile(null, null, id);
    }

    private String parseBookForElastic(MultipartFile multipartFile) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("digital-lib", "tempFile");
            tempFile.deleteOnExit();
            multipartFile.transferTo(tempFile);
            File xmlFile = new File("path/to/your/xml/file.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            doc.getDocumentElement().normalize();

            // Получаем корневой элемент
            Element rootElement = doc.getDocumentElement();
            NodeList bodyList = rootElement.getElementsByTagNameNS("http://www.gribuser.ru/xml/fictionbook/2.0", "body");

            if (bodyList.getLength() > 0) {
                return bodyList.item(0).getTextContent();
            } else {
                throw ClientException.of(HttpStatus.BAD_REQUEST, "Файл книги пустой");
            }

        } catch (Exception e) {
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось сохранить файл книги");
        } finally {
            Optional.of(tempFile).ifPresent(File::delete);
        }
    }
}
