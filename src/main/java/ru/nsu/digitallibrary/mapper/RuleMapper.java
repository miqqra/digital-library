package ru.nsu.digitallibrary.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.nsu.digitallibrary.dto.rule.AddRuleDto;
import ru.nsu.digitallibrary.dto.rule.RuleDto;
import ru.nsu.digitallibrary.entity.SearchRule;
import ru.nsu.digitallibrary.model.RuleModel;

@Mapper
public abstract class RuleMapper {

    public abstract RuleModel toModel(SearchRule source);

    public abstract RuleDto toDto(RuleModel source);

    public abstract SearchRule toEntity(RuleModel source);

    @Mapping(target = "id", ignore = true)
    public abstract RuleModel toModel(AddRuleDto source);

    public abstract RuleModel toModel(RuleDto source);

}
