package ru.nsu.digitallibrary.repository.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.digitallibrary.entity.BookFile;

@Repository
public interface BookFileElasticSearchRepository extends ElasticsearchRepository<BookFile, Long> {

    boolean existsBookFileByBookId(Long bookId);

    void deleteBookFileByBookId(Long bookId);
}
