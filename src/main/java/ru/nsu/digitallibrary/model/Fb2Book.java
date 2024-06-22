package ru.nsu.digitallibrary.model;

import com.kursx.parser.fb2.Body;
import com.kursx.parser.fb2.Description;
import com.kursx.parser.fb2.FictionBook;
import com.kursx.parser.fb2.P;
import com.kursx.parser.fb2.Section;
import com.kursx.parser.fb2.TitleInfo;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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


    public Fb2Book(MultipartFile multipart, HttpServletRequest request) {
        try {
            String filePath = request.getServletContext().getRealPath("/");
            File f1 = new File(filePath + "/" + multipart.getOriginalFilename());
            multipart.transferTo(f1);

            book = new FictionBook(f1);
        } catch (Exception e) {
            throw ClientException.of(HttpStatus.BAD_REQUEST, "Не удалось получить данные из книги");
        }
    }

    public List<String> getParagraphs() {
        return Optional.of(book)
                .map(FictionBook::getBody)
                .map(Body::getSections)
                .map(this::addSections)
                .orElse(Collections.emptyList());
    }

    private List<String> addSections(List<Section> sections) {
        List<String> paragraphs = sections.stream()
                .map(Section::getParagraphs)
                .flatMap(Collection::stream)
                .map(P::getP)
                .collect(Collectors.toList());

        List<String> innerSections = sections.stream()
                .map(Section::getSections)
                .map(this::addSections)
                .flatMap(Collection::stream)
                .toList();

        paragraphs.addAll(innerSections);
        return paragraphs;
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
                .map(v -> List.of(v.getFirstName(), v.getMiddleName(), v.getLastName()))
                .flatMap(Collection::stream)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(", "));
    }

    public String getGenres() {
        return String.join(" ", Optional.of(book)
                .map(FictionBook::getDescription)
                .map(Description::getTitleInfo)
                .map(TitleInfo::getGenres)
                .stream()
                .flatMap(Collection::stream)
                .map(Genres::getValueByGenreCode)
                .map(Genres::getName)
                .toList());
    }
}
