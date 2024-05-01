package ru.nsu.digitallibrary.model;


import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
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

    TITLE(searchText -> ESQuery.matchWithPrefix.getQuery().apply("book.title", searchText)),
    AUTHOR(searchText -> ESQuery.matchWithPrefix.getQuery().apply("book.author", searchText)),
    GENRE(searchText -> ESQuery.matchWithPrefix.getQuery().apply("book.genre", searchText)),
    DESCRIPTION(searchText -> ESQuery.matchWithPrefix.getQuery().apply("book.description", searchText)),
    ISBN(searchText -> ESQuery.term.getQuery().apply("book.title", searchText)),
    DATA(searchText -> ESQuery.match.getQuery().apply("book.data", searchText)),
    SCORE(searchText -> ESQuery.scoreFilter.getQuery().apply("book.score", searchText));

    private final Function<String, Query> query;

    public static NativeQuery getQueryForStrategy(List<SearchFacetDto> facets) {
        return NativeQuery.builder().withQuery(Query.of(q -> q.bool(b -> b.must(
                facets
                        .stream()
                        .map(v -> v
                                .getStrategy()
                                .getQuery()
                                .apply(v.getSearchText()))
                        .toList()
        )))).build();
    }

    @Getter
    @RequiredArgsConstructor
    private enum ESQuery {

        matchWithPrefix(
                (fieldName, searchText) -> Query.of(q -> q.bool(b -> b.should(
                        List.of(
                                Query.of(q1 -> q1.matchPhrasePrefix(m -> m
                                        .query(searchText)
                                        .field(fieldName))),
                                Query.of(q1 -> q1.match(m -> m
                                        .query(searchText)
                                        .field(fieldName)
                                        .fuzziness("AUTO")))
                        )
                )))
        ),

        match(
                (fieldName, searchText) -> Query.of(q -> q.match(m -> m
                        .query(searchText)
                        .field(fieldName)
                        .fuzziness("AUTO"))
                )
        ),

        term(
                (fieldName, searchText) -> Query.of(q -> q.term(t -> t
                        .field(fieldName)
                        .value(searchText)
                ))
        ),

        scoreFilter(
                (fieldName, searchText) -> Query.of(q -> q.range(r -> r
                                .field(fieldName)
                                .gte(JsonData.of("0.0"))
                                .lte(JsonData.of(searchText))
                        )
                )
        );

        private final BiFunction<String, String, Query> query;

    }
}
