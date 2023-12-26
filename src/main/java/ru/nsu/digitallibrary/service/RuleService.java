package ru.nsu.digitallibrary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.nsu.digitallibrary.dto.rule.AddRuleDto;
import ru.nsu.digitallibrary.dto.rule.RuleDto;
import ru.nsu.digitallibrary.mapper.RuleMapper;
import ru.nsu.digitallibrary.service.data.RuleDataService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleDataService dataService;

    private final RuleMapper mapper;

    public List<RuleDto> getRules() {
        return dataService.getRules()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public RuleDto getRule(String category) {
        return Optional.of(category)
                .map(dataService::getRule)
                .map(mapper::toDto)
                .orElse(null);
    }

    public RuleDto addRule(AddRuleDto dto) {
        return Optional.of(dto)
                .map(mapper::toModel)
                .map(dataService::addRule)
                .map(mapper::toDto)
                .orElse(null);
    }

    public RuleDto updateRule(RuleDto dto) {
        return Optional.of(dto)
                .map(mapper::toModel)
                .map(dataService::updateRule)
                .map(mapper::toDto)
                .orElse(null);
    }

    public void deleteRule(@PathVariable Long id) {
        dataService.deleteRule(id);
    }

}
