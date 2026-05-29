package com.transpiler.parser;

import com.transpiler.ir.ProgramNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Placeholder parser for Java.
 */
public class JavaParser implements LanguageParser {
    private static final Logger logger = LoggerFactory.getLogger(JavaParser.class);

    @Override
    public ProgramNode parse(String sourceCode) {
        if (sourceCode == null) {
            throw new IllegalArgumentException("Source code cannot be null");
        }
        logger.info("Parsing Java code...");
        throw new UnsupportedOperationException("Java parsing is not yet implemented.");
    }
}
