package ru.nsu.digitallibrary.model;

import lombok.Data;

@Data
public class RuleModel {

    private Long id;
    private String category;
    private String query;
}
