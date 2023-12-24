package ru.nsu.digitallibrary.repository.elasticsearch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nsu.digitallibrary.entity.postgres.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Book findBookById(Long id);

    Book findBookByIsbn(String isbn);

    @Modifying
    @Query("""
update Book set file = :file where id = :id
""")
    void updateBookFile(byte[] file, Long id);




}
