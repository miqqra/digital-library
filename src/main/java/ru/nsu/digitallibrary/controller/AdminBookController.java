package ru.nsu.digitallibrary.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.nsu.digitallibrary.dto.book.AddBookDto;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.service.BookService;

@RestController
@RequestMapping("admin/api/book")
@RequiredArgsConstructor
public class AdminBookController {

    private final BookService service;

    @PutMapping
    @Operation(summary = "Обновить данные о книге")
    public BookDto updateBook(@RequestBody BookDto bookDto) {
        return service.updateBook(bookDto);
    }

    @PostMapping
    @Operation(summary = "Добавить книгу")
    public BookDto addBook(@RequestBody AddBookDto bookDto) {
        return service.addBook(bookDto);
    }

    @PostMapping(path = "/add",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Добавить книгу")
    public BookDto addBook(@RequestParam MultipartFile file) {
        return service.addBook(file);
    }

    @PutMapping(path = "/{id}/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузить файл книги")
    public void uploadFile(@PathVariable String id, @RequestParam MultipartFile file) {
        service.uploadFile(id, file);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить книгу")
    public void deleteBook(@PathVariable String id) {
        service.deleteBook(id);
    }

    @DeleteMapping("/{id}/file")
    @Operation(summary = "Удалить файл книги")
    public void deleteBookFile(@PathVariable String id) {
        service.deleteBookFile(id);
    }
}
