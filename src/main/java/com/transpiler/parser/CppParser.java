package com.transpiler.parser;

import com.transpiler.ir.ProgramNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Placeholder parser for C++.
 */
public class CppParser implements LanguageParser {
    private static final Logger logger = LoggerFactory.getLogger(CppParser.class);

    @Override
    public ProgramNode parse(String sourceCode) {
        if (sourceCode == null) {
            throw new IllegalArgumentException("Source code cannot be null");
        }
        logger.info("Parsing C++ code...");
        throw new UnsupportedOperationException("C++ parsing is not yet implemented.");
    }
}
