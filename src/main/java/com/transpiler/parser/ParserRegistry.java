package com.transpiler.parser;

import com.transpiler.utils.Language;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registry for managing and retrieving language-specific parsers.
 */
public class ParserRegistry {
    private static final Map<Language, LanguageParser> parsers = new EnumMap<>(Language.class);

    static {
        parsers.put(Language.PYTHON, new PythonParser());
        parsers.put(Language.JAVA, new JavaParser());
        parsers.put(Language.C, new CParser());
        parsers.put(Language.CPP, new CppParser());
        parsers.put(Language.JAVASCRIPT, new JavaScriptParser());
        parsers.put(Language.TYPESCRIPT, new TypeScriptParser());
        parsers.put(Language.RUBY, new RubyParser());
    }

    private ParserRegistry() {
    }

    /**
     * Retrieves a parser for the specified language.
     *
     * @param language The language to get a parser for.
     * @return The LanguageParser instance.
     * @throws IllegalArgumentException if no parser is registered for the language.
     */
    public static LanguageParser getParser(Language language) {
        LanguageParser parser = parsers.get(language);
        if (parser == null) {
            throw new IllegalArgumentException("No parser registered for language: " + language);
        }
        return parser;
    }
}
