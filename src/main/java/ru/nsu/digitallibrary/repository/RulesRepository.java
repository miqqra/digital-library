package ru.nsu.digitallibrary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.digitallibrary.entity.postgres.SearchRule;

@Repository
public interface RulesRepository extends JpaRepository<SearchRule, Long> {

    SearchRule findByCategory(String category);

}
