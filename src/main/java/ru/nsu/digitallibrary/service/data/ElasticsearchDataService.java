package ru.nsu.digitallibrary.service.data;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
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
}
