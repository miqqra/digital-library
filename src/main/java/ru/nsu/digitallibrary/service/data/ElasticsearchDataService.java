package ru.nsu.digitallibrary.service.data;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.stereotype.Service;
import ru.nsu.digitallibrary.dto.book.BookDto;
import ru.nsu.digitallibrary.dto.search.SearchFacetDto;
import ru.nsu.digitallibrary.entity.elasticsearch.BookData;
import ru.nsu.digitallibrary.mapper.BookMapper;
import ru.nsu.digitallibrary.model.ElasticsearchFindStrategy;
import ru.nsu.digitallibrary.repository.elasticsearch.BookDataElasticSearchRepository;

@Service
@RequiredArgsConstructor
public class ElasticsearchDataService {

    private final BookDataElasticSearchRepository repository;

    private final ElasticsearchOperations elasticsearchOperations;

    private final BookMapper mapper;

    public List<BookDto> findBooks(List<SearchFacetDto> facets) {
        return Optional.of(facets)
                .map(ElasticsearchFindStrategy::getQueryForStrategy)
                .map(query -> elasticsearchOperations.search(query, BookData.class))
                .map(SearchHits::getSearchHits)
                .stream()
                .flatMap(Collection::stream)
                .map(SearchHit::getContent)
                .map(mapper::toDto)
                .toList();
    }

    public List<String> findAllBookIds() {
        return Optional.of(getAllBookIdsQuery())
                .map(query -> elasticsearchOperations.search(query, BookData.class))
                .map(SearchHits::getSearchHits)
                .stream()
                .flatMap(Collection::stream)
                .map(SearchHit::getId)
                .toList();
    }

    public List<BookDto> findAllShortenedBooks() {
        return Optional.of(getAllBooksQuery())
                .map(query -> elasticsearchOperations.search(query, BookData.class))
                .map(SearchHits::getSearchHits)
                .stream()
                .flatMap(Collection::stream)
                .map(SearchHit::getContent)
                .map(mapper::toDto)
                .toList();
    }

    public BookDto findShortenedById(String id) {
        return Optional.of(id)
                .map(this::findByIdQuery)
                .map(query -> elasticsearchOperations.search(query, BookData.class))
                .map(SearchHits::getSearchHits)
                .stream()
                .flatMap(Collection::stream)
                .map(SearchHit::getContent)
                .map(mapper::toDto)
                .findFirst()
                .orElse(null);
    }

    public BookData findById(String id) {
        return Optional.of(id)
                .flatMap(repository::findById)
                .orElse(null);
    }

    public BookData save(BookData bookData) {
        return Optional.of(bookData)
                .map(repository::save)
                .orElse(null);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public BookData findByAuthorAndTitle(String author, String title) {
        return Optional.of(author)
                .map(v -> repository.findFirstByAuthorAndTitle(v, title))
                .orElse(null);
    }

    private NativeQuery getAllBookIdsQuery() {
        return NativeQuery.builder()
                .withQuery(Query.of(
                        q -> q.matchAll(qma -> qma)))
                .withSourceFilter(new FetchSourceFilter(
                        new String[]{"_id"},
                        new String[]{}
                ))
                .build()
                .setPageable(Pageable.ofSize(1000));
    }

    private NativeQuery getAllBooksQuery() {
        return NativeQuery.builder()
                .withQuery(Query.of(
                        q -> q.matchAll(qma -> qma)))
                .withSourceFilter(new FetchSourceFilter(
                        new String[]{},
                        new String[]{"data"}
                ))
                .build();
    }

    private NativeQuery findByIdQuery(String id) {
        return NativeQuery.builder()
                .withQuery(Query.of(
                        q -> q.term(
                                t -> t
                                        .field("_id")
                                        .value(id))
                ))
                .withSourceFilter(new FetchSourceFilter(
                        new String[]{},
                        new String[]{"data"}
                ))
                .build();
    }
}
