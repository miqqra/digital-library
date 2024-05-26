package ru.nsu.digitallibrary.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.digitallibrary.dto.rule.AddRuleDto;
import ru.nsu.digitallibrary.dto.rule.RuleDto;
import ru.nsu.digitallibrary.service.RuleService;

@RestController
@RequestMapping("admin/api/rules")
@RequiredArgsConstructor
public class AdminSearchRulesController {

    private final RuleService service;

    //todo delete
    @GetMapping
    public List<RuleDto> getRules() {
        return service.getRules();
    }

    @GetMapping("/{category}")
    public RuleDto getRule(@PathVariable String category) {
        return service.getRule(category);
    }

    @PostMapping
    public RuleDto addRule(@RequestBody AddRuleDto dto) {
        return service.addRule(dto);
    }

    @PutMapping
    public RuleDto updateRule(@RequestBody RuleDto dto) {
        return service.updateRule(dto);
    }

    @DeleteMapping
    public void deleteRule(@PathVariable Long id) {
        service.deleteRule(id);
    }
}
