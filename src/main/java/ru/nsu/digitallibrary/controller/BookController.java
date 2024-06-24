package ru.nsu.digitallibrary.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping(value = "/{id}/download",
            produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Скачать книгу")
    public ResponseEntity<Resource> downloadBook(@PathVariable @Parameter(description = "Id книги") String id) {
        byte[] bytes = service.downloadBook(id);
        ByteArrayResource resource = new ByteArrayResource(bytes);

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=book.fb2");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(header)
                .contentLength(bytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/id")
    @Operation(summary = "Получить все id книг")
    public BookIdsDto getBookIds() {
        return service.getBookIds();
    }

    @GetMapping(value = "/text")
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
