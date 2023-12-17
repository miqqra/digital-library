package ru.nsu.digitallibrary.dto.rule;

import lombok.Data;

@Data
public class RuleDto {

    private Long id;
    private String category;
    private String query;

}
