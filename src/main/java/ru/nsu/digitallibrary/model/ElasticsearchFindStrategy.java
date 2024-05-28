package ru.nsu.digitallibrary.model;


import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import ru.nsu.digitallibrary.dto.search.SearchFacetDto;

@Getter
@RequiredArgsConstructor
public enum ElasticsearchFindStrategy {

    TITLE(searchText -> ESQuery.matchWithPrefix.getQuery().apply("title", searchText)),
    AUTHOR(searchText -> ESQuery.matchWithPrefix.getQuery().apply("author", searchText)),
    GENRE(searchText -> ESQuery.matchWithPrefix.getQuery().apply("genre", searchText)),
    DESCRIPTION(searchText -> ESQuery.matchWithPrefix.getQuery().apply("description", searchText)),
    ISBN(searchText -> ESQuery.term.getQuery().apply("isbn", searchText)),
    DATA(searchText -> ESQuery.match.getQuery().apply("data", searchText)),
    SCORE(searchText -> ESQuery.scoreFilter.getQuery().apply("score", searchText));

    private final Function<String, List<Query>> query;

    public static NativeQuery getQueryForStrategy(List<SearchFacetDto> facets) {

        return NativeQuery.builder().withQuery(Query.of(q -> q.bool(b -> b.should(
                facets
                        .stream()
                        .map(v -> v
                                .getStrategy()
                                .getQuery()
                                .apply(v.getSearchText()))
                        .flatMap(Collection::stream)
                        .toList()
        )))).build();
    }

    @Getter
    @RequiredArgsConstructor
    private enum ESQuery {

        matchWithPrefix(
                (fieldName, searchText) -> List.of(
                        Query.of(q1 -> q1.matchPhrasePrefix(m -> m
                                .query(searchText)
                                .field(fieldName))),
                        Query.of(q1 -> q1.match(m -> m
                                .query(searchText)
                                .field(fieldName)
                                .fuzziness("AUTO"))
                        ))
        ),

        match(
                (fieldName, searchText) -> List.of(
                        Query.of(q -> q.match(m -> m
                                .query(searchText)
                                .field(fieldName)
                                .fuzziness("AUTO"))
                        ))
        ),

        term(
                (fieldName, searchText) -> List.of(
                        Query.of(q -> q.term(t -> t
                                .field(fieldName)
                                .value(searchText))
                        ))
        ),

        scoreFilter(
                (fieldName, searchText) -> List.of(
                        Query.of(q -> q.range(r -> r
                                .field(fieldName)
                                .gte(JsonData.of("0.0"))
                                .lte(JsonData.of(searchText))
                        )))
        );

        private final BiFunction<String, String, List<Query>> query;

    }
}
