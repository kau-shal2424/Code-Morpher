package com.transpiler.parser;

import com.transpiler.ir.ProgramNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Placeholder parser for Ruby.
 */
public class RubyParser implements LanguageParser {
    private static final Logger logger = LoggerFactory.getLogger(RubyParser.class);

    @Override
    public ProgramNode parse(String sourceCode) {
        if (sourceCode == null) {
            throw new IllegalArgumentException("Source code cannot be null");
        }
        logger.info("Parsing Ruby code...");
        throw new UnsupportedOperationException("Ruby parsing is not yet implemented.");
    }
}
