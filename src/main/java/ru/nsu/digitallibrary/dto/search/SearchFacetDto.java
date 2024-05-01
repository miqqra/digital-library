package ru.nsu.digitallibrary.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.nsu.digitallibrary.model.ElasticsearchFindStrategy;

@Data
public class SearchFacetDto {

    @NotNull
    @Schema(description = "Стратегия поиска. Доступные фасеты: TITLE, AUTHOR, GENRE, DESCRIPTION, ISBN, DATA, SCORE")
    private ElasticsearchFindStrategy strategy;

    @NotNull
    @Schema(description = "Текст для поиска")
    private String searchText;
}
