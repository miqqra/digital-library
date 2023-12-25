package ru.nsu.digitallibrary.service;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.digitallibrary.entity.elasticsearch.BookData;
import ru.nsu.digitallibrary.exception.ClientException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ElasticsearchBookService {

    @Value("${app.words.weight.base:1}")
    private Integer baseWordsWeight;

    @Value("${app.words.weight.extended:1}")
    private Integer extendedWordsWeight;

    //private final ElasticsearchOperations elasticsearchOperations;

    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Transactional(readOnly = true)
    public List<Long> searchBook(String searchQuery) {
        try {
            String pythonScriptPath = "C:\\Users\\User\\IdeaProjects\\digital-library\\src\\main\\resources\\script.py";

            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath, searchQuery);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            Map<String, Integer> wordsWeights = formatExtendedQuery(output);
            return searchWithWeightedWords(wordsWeights);
        } catch (IOException e) {
            throw ClientException.of(HttpStatus.NOT_FOUND, "Не удалось произвести поиск");
        }
    }

    private Map<String, Integer> formatExtendedQuery(String query) {
        Map<String, Integer> wordsWeights = new HashMap<>();
        String[] output = query.split("\\s+");
        int meaningfulWordsNumber = Integer.parseInt(output[0]);
        for (int i = 1; i < output.length && i <= meaningfulWordsNumber; i++) {
            wordsWeights.put(output[i], baseWordsWeight);
        }
        for (int i = meaningfulWordsNumber + 1; i < output.length; i++) {
            if (!wordsWeights.containsKey(output[i])) {
                wordsWeights.put(output[i], extendedWordsWeight);
            }
        }
        return wordsWeights;
    }

    private List<Long> searchWithWeightedWords(Map<String, Integer> wordWeightMap) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(buildFunctionScoreQuery(wordWeightMap))
                .build();

        SearchHits<BookData> searchHits = elasticsearchRestTemplate.search(searchQuery, BookData.class);

        return searchHits
                .get()
                .map(SearchHit::getContent)
                .map(BookData::getBookId)
                .toList();
    }

    private FunctionScoreQueryBuilder buildFunctionScoreQuery(Map<String, Integer> wordWeightMap) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        for (Map.Entry<String, Integer> entry : wordWeightMap.entrySet()) {
            String word = entry.getKey();
            Integer weight = entry.getValue();

            boolQueryBuilder.should(QueryBuilders.matchQuery("text", word).boost(weight.floatValue()));
        }

        return QueryBuilders.functionScoreQuery(boolQueryBuilder)
                //.add(ScoreFunctionBuilders.weightFactorFunction(1))
                ;
    }
}
