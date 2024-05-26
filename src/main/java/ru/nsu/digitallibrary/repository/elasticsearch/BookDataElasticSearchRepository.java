package ru.nsu.digitallibrary.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.digitallibrary.entity.elasticsearch.BookData;

@Repository
public interface BookDataElasticSearchRepository extends ElasticsearchRepository<BookData, String> {

    BookData findByAuthorAndTitle(String author, String title);
}
