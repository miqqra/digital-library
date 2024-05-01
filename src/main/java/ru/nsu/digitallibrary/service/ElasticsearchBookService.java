//package ru.nsu.digitallibrary.service;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
//import org.springframework.data.elasticsearch.core.SearchHit;
//import org.springframework.data.elasticsearch.core.SearchHits;
//import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
//import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import ru.nsu.digitallibrary.entity.elasticsearch.BookData;
//import ru.nsu.digitallibrary.exception.ClientException;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ElasticsearchBookService {
//    //todo delete
//
//    @Value("${app.words.weight.base:1}")
//    private Integer baseWordsWeight;
//
//    @Value("${app.words.weight.extended:1}")
//    private Integer extendedWordsWeight;
//
//    @Value("${app.books.number:10}")
//    private Integer searchedBooksLimit;
//
//    //private final ElasticsearchOperations elasticsearchOperations;
//
//    private final ElasticsearchRestTemplate elasticsearchRestTemplate;
//
//    @Transactional(readOnly = true)
//    public List<Long> searchBook(String searchQuery) {
//        try {
//            String pythonScriptPath = "C:\\Users\\User\\IdeaProjects\\digital-library\\src\\main\\resources\\QueryExtender\\main.py";
//
//            ProcessBuilder processBuilder = new ProcessBuilder(
//                    "C:\\Users\\User\\IdeaProjects\\digital-library\\src\\main\\resources\\QueryExtender\\venv\\Scripts\\python.exe",
//                    pythonScriptPath,
//                    searchQuery);
//            Process process = processBuilder.start();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String output = reader.readLine();
//            Map<String, Integer> wordsWeights = formatExtendedQuery(output, searchQuery);
//            return searchWithWeightedWords(wordsWeights);
//        } catch (IOException e) {
//            throw ClientException.of(HttpStatus.NOT_FOUND, "Не удалось произвести поиск");
//        }
//    }
//
//    private Map<String, Integer> formatExtendedQuery(String query, String defaultQuery) {
//        Map<String, Integer> wordsWeights = new HashMap<>();
//
//        String[] output;
//        int meaningfulWordsNumber;
//        if (query == null) {
//            output = defaultQuery.split("\\s+");
//            meaningfulWordsNumber = output.length;
//        } else {
//            output = query.split("\\s+");
//            meaningfulWordsNumber = Integer.parseInt(output[0]);
//        }
//
//        for (int i = 1; i < output.length && i <= meaningfulWordsNumber; i++) {
//            wordsWeights.put(output[i], baseWordsWeight);
//        }
//        for (int i = meaningfulWordsNumber + 1; i < output.length; i++) {
//            if (!wordsWeights.containsKey(output[i])) {
//                wordsWeights.put(output[i], extendedWordsWeight);
//            }
//        }
//        return wordsWeights;
//    }
//
//    private List<Long> searchWithWeightedWords(Map<String, Integer> wordWeightMap) {
//        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(buildFunctionScoreQuery(wordWeightMap))
//                .build();
//
//        SearchHits<BookData> searchHits = elasticsearchRestTemplate.search(searchQuery, BookData.class);
//
//        searchHits.getSearchHits()
//                .stream()
//                .limit(10)
//                .peek(v -> log.info("{} scored {}", v.getContent().getBookId(), v.getScore()));
//
//        return searchHits
//                .get()
//                .map(SearchHit::getContent)
//                .map(BookData::getBookId)
//                .limit(searchedBooksLimit)
//                .toList();
//    }
//
//    private FunctionScoreQueryBuilder buildFunctionScoreQuery(Map<String, Integer> wordWeightMap) {
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//
//        for (Map.Entry<String, Integer> entry : wordWeightMap.entrySet()) {
//            String word = entry.getKey();
//            Integer weight = entry.getValue();
//
//            boolQueryBuilder.should(QueryBuilders.matchQuery("data", word).boost(weight.floatValue()));
//        }
//
//        return QueryBuilders.functionScoreQuery(boolQueryBuilder);
//    }
//}
