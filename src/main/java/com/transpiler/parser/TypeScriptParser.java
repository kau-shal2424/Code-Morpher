package com.transpiler.parser;

import com.transpiler.ir.ProgramNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Placeholder parser for TypeScript.
 */
public class TypeScriptParser implements LanguageParser {
    private static final Logger logger = LoggerFactory.getLogger(TypeScriptParser.class);

    @Override
    public ProgramNode parse(String sourceCode) {
        if (sourceCode == null) {
            throw new IllegalArgumentException("Source code cannot be null");
        }
        logger.info("Parsing TypeScript code...");
        throw new UnsupportedOperationException("TypeScript parsing is not yet implemented.");
    }
}
