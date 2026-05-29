package com.transpiler.parser;

import com.transpiler.ir.ProgramNode;

/**
 * Interface for language-specific parsers.
 */
public interface LanguageParser {
    /**
     * Parses source code into a common IR representation.
     *
     * @param sourceCode The source code to parse.
     * @return The ProgramNode representing the parsed code.
     */
    ProgramNode parse(String sourceCode);
}
