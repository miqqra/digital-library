package ru.nsu.digitallibrary.model;

import com.kursx.parser.fb2.FictionBook;
import com.kursx.parser.fb2.P;
import com.kursx.parser.fb2.Section;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import ru.nsu.digitallibrary.exception.ClientException;

@RequiredArgsConstructor
@Data
public class Fb2Book {

    private final FictionBook book;

    public Fb2Book(MultipartFile multipart) {
        try {
            File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipart.getName());
            multipart.transferTo(file);
            book = new FictionBook(file);
        } catch (Exception e) {
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось получить данные из книги");
        }
    }

    public List<String> getParagraphs() {
        return book.getBody().getSections()
                .stream()
                .map(Section::getParagraphs)
                .flatMap(Collection::stream)
                .map(P::getP)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    public String getData() {
        return String.join(" ", getParagraphs());

    }

    public String getTitle() {
        return book.getDescription().getTitleInfo().getBookTitle();
    }

    public String getAuthors() {
        return book
                .getDescription()
                .getTitleInfo()
                .getAuthors()
                .stream()
                .map(v -> String.join(" ", v.getFirstName(), v.getMiddleName(), v.getLastName()))
                .collect(Collectors.joining(", "));
    }

    public String getGenres() {
        return String.join(" ", book.getDescription().getTitleInfo().getGenres());
    }
}
