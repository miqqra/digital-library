package ru.nsu.digitallibrary.service.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.nsu.digitallibrary.mapper.RuleMapper;
import ru.nsu.digitallibrary.model.RuleModel;
import ru.nsu.digitallibrary.repository.postgres.RulesRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RuleDataService {

    private final RulesRepository repository;

    private final RuleMapper mapper;

    public List<RuleModel> getRules() {
        return repository
                .findAll()
                .stream()
                .map(mapper::toModel)
                .toList();
    }

    public RuleModel getRule(String category) {
        return Optional.of(category)
                .map(repository::findByCategory)
                .map(mapper::toModel)
                .orElse(null);
    }

    public RuleModel addRule(RuleModel ruleModel) {
        return Optional.of(ruleModel)
                .map(mapper::toEntity)
                .map(repository::save)
                .map(mapper::toModel)
                .orElse(null);
    }

    public RuleModel updateRule(RuleModel ruleModel) {
        return Optional.of(ruleModel)
                .map(mapper::toEntity)
                .map(repository::save)
                .map(mapper::toModel)
                .orElse(null);
    }

    public void deleteRule(@PathVariable Long id) {
        repository.deleteById(id);
    }

}
