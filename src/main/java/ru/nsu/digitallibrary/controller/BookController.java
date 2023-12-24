package ru.nsu.digitallibrary.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.service.BookService;

import java.util.List;

@RestController
@RequestMapping("api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @GetMapping("/search")
    public List<BookDto> searchBook(@RequestParam String searchQuery) {
        return service.searchBook(searchQuery);
    }

    @GetMapping("/{id}/download")
    public byte[] downloadBook(@PathVariable Long id) {
        return service.downloadBook(id);
    }

    @GetMapping
    public BookDto getBookData(@RequestParam Long id) {
        return service.getBookData(id);
    }
}
