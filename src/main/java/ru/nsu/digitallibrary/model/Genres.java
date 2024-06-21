package ru.nsu.digitallibrary.model;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;

@RequiredArgsConstructor
@Getter
public enum Genres {

    ADV_ANIMAL("Приключения о животных"),
    ADV_HISTORY("Исторические приключения"),
    ADV_MARITIME("Морские приключения"),
    ADVENTURE("Приключения"),
    ANTIQUE_EAST("Античная восточная литература"),
    ANTIQUE_EUROPEAN("Античная европейская литература"),
    CHILD_PROSE("Детская проза"),
    DET_CLASSIC("Классический детектив"),
    DET_ESPIONAGE("Шпионский детектив"),
    DET_HARD("Крутой детектив"),
    DET_HISTORY("Исторический детектив"),
    DET_MANIAC("Маньяческий детектив"),
    DETECTIVE("Детектив"),
    DRAMATURGY("Драматургия"),
    HUMOR_PROSE("Юмористическая проза"),
    HUMOR_VERSE("Юмористическая поэзия"),
    LITERATURE_CLASSICS("Классическая литература"),
    LOVE_CONTEMPORARY("Современные любовные романы"),
    LOVE_DETECTIVE("Любовные детективы"),
    LOVE_HISTORYM("Исторические любовные романы"),
    NONF_BIOGRAPHY("Биографии"),
    NONF_PUBLICISM("Публицистика"),
    POETRY("Поэзия"),
    PROSE_CLASSIC("Классическая проза"),
    PROSE_CONTEMPORARY("Современная проза"),
    PROSE_COUNTER("Контркультурная проза"),
    PROSE_HISTORY("Историческая проза"),
    PROSE_RUS_CLASSIC("Классическая русская проза"),
    RELIGION_REL("Религиозная литература"),
    ROMANCE("Романы"),
    SCI_PHILOSOPHY("Философская литература"),
    SCI_PSYCHOLOGY("Психологическая литература"),
    SF("Научная фантастика"),
    SF_HISTORY("Историческая фантастика"),
    SF_HORROR("Ужасы"),
    SF_HUMOR("Юмористическая фантастика"),
    SF_SOCIAL("Социальная фантастика"),
    THRILLER("Триллер"),
    TRAVEL_AFRICA("Путешествия по Африке");

    private final String name;

    public static Genres getValueByGenreName(String name) {
        return Arrays.stream(Genres.values())
                .filter(v -> v.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static Genres getValueByGenreCode(String code) {
        return Optional.of(code)
                .map(String::toUpperCase)
                .map(v -> EnumUtils.getEnum(Genres.class, v))
                .orElse(null);
    }
}
