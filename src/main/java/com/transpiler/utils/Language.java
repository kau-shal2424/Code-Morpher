package com.transpiler.utils;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enum representing supported programming languages.
 */
public enum Language {
    PYTHON("python"),
    JAVA("java"),
    C("c"),
    CPP("cpp"),
    JAVASCRIPT("javascript"),
    TYPESCRIPT("typescript"),
    RUBY("ruby");

    private final String name;

    Language(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Converts a string to a Language enum case-insensitively.
     *
     * @param value The language name as a string.
     * @return An Optional containing the Language if found, or empty if not.
     */
    public static Optional<Language> fromString(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(lang -> lang.name.equalsIgnoreCase(value) || lang.name().equalsIgnoreCase(value))
                .findFirst();
    }

    /**
     * Validates if a string corresponds to a supported language.
     *
     * @param value The language name.
     * @throws IllegalArgumentException if the language is not supported.
     */
    public static Language validate(String value) {
        return fromString(value)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported language: " + value + 
                    ". Supported languages: " + Arrays.toString(values())));
    }

    @Override
    public String toString() {
        return name;
    }
}
