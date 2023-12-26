package ru.nsu.digitallibrary.controller;

import lombok.RequiredArgsConstructor;
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
    public BookDto updateBook(@RequestBody BookDto bookDto) {
        return service.updateBook(bookDto);
    }

    @PostMapping
    public BookDto addBook(@RequestBody AddBookDto bookDto) {
        return service.addBook(bookDto);
    }

    @PutMapping("/{id}/upload")
    public void uploadFile(@PathVariable Long id, @RequestParam MultipartFile file) {
        service.uploadFile(id, file);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        service.deleteBook(id);
    }

    @DeleteMapping("/{id}/file")
    public void deleteBookFile(@PathVariable Long id) {
        service.deleteBookFile(id);
    }
}
