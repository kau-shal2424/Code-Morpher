package com.transpiler.generator;

import com.transpiler.utils.Language;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registry for managing and retrieving language-specific code generators.
 */
public class GeneratorRegistry {
    private static final Map<Language, CodeGenerator> generators = new EnumMap<>(Language.class);

    static {
        generators.put(Language.PYTHON, new PythonGenerator());
        generators.put(Language.JAVA, new JavaGenerator());
        generators.put(Language.C, new CGenerator());
        generators.put(Language.CPP, new CppGenerator());
        generators.put(Language.JAVASCRIPT, new JavaScriptGenerator());
        generators.put(Language.TYPESCRIPT, new TypeScriptGenerator());
        generators.put(Language.RUBY, new RubyGenerator());
    }

    private GeneratorRegistry() {
    }

    /**
     * Retrieves a generator for the specified language.
     *
     * @param language The language to get a generator for.
     * @return The CodeGenerator instance.
     * @throws IllegalArgumentException if no generator is registered for the language.
     */
    public static CodeGenerator getGenerator(Language language) {
        CodeGenerator generator = generators.get(language);
        if (generator == null) {
            throw new IllegalArgumentException("No generator registered for language: " + language);
        }
        return generator;
    }
}
