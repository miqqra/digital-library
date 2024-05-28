package ru.nsu.digitallibrary.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.dto.book.BookIdsDto;
import ru.nsu.digitallibrary.dto.book.BookTextDto;
import ru.nsu.digitallibrary.dto.search.SearchFacetDto;
import ru.nsu.digitallibrary.service.BookService;

@RestController
@RequestMapping("api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @PutMapping("/search")
    @Operation(summary = "Поиск книги по фасетам")
    public List<BookDto> searchBook(@RequestBody @Validated List<SearchFacetDto> facets) {
        return service.searchBook(facets);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Скачать книгу")
    public byte[] downloadBook(@PathVariable @Parameter(description = "Id книги") String id) {
        return service.downloadBook(id);
    }

    @GetMapping("/id")
    @Operation(summary = "Получить все id книг")
    public BookIdsDto getBookIds() {
        return service.getBookIds();
    }

    @GetMapping("/text")
    @Operation(summary = "Получить текст книги")
    public BookTextDto getBookText(@RequestParam String id) {
        return service.getBookText(id);
    }


    @GetMapping
    @Operation(summary = "Получить данные о книге")
    public BookDto getBookData(@RequestParam @Parameter(description = "Id книги") String id) {
        return service.getBookData(id);
    }

    @PutMapping("/rate")
    @Operation(summary = "Оценить книгу")
    public void rateBook(@RequestParam @Parameter(description = "Id книги") String id,
                         @RequestParam @Parameter(description = "Оценка книге") Double score) {
        service.rateBook(id, score);
    }
}
