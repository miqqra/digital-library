package ru.nsu.digitallibrary.repository.elasticsearch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.digitallibrary.entity.Book;

@Repository
public interface BookElasticSearchRepository extends JpaRepository<Book, Long> {

    Book findBookById(Long id);

    Book findBookByIsbn(String isbn);

    Boolean existsBookById(Long id);


}
