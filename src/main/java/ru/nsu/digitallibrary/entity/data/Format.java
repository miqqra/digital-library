package ru.nsu.digitallibrary.entity.data;

import java.util.Optional;

public enum Format {

    FB2,
    EPUB,
    TXT,
    PDF;

    public String getName(Format format) {
        return Optional.of(format)
                .map(this::getName)
                .orElseThrow(() -> new IllegalStateException("Некорректный формат файла"));
    }

    public Format getEnum(String name) {
        return Optional.of(name)
                .map(this::getEnum)
                .orElseThrow(() -> new IllegalStateException("Некорректный формат файла"));
    }
}
