package com.transpiler.parser;

import com.transpiler.dto.CompilerErrorDto;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom ANTLR error listener to capture syntax errors for the visualization suite.
 */
public class CompilerErrorListener extends BaseErrorListener {
    private final List<CompilerErrorDto> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg, RecognitionException e) {
        int startIndex = 0;
        int stopIndex = 0;
        if (offendingSymbol instanceof Token) {
            Token token = (Token) offendingSymbol;
            startIndex = token.getStartIndex();
            stopIndex = token.getStopIndex();
        }
        errors.add(new CompilerErrorDto(line, charPositionInLine, msg, "ERROR", startIndex, stopIndex));
    }

    public List<CompilerErrorDto> getErrors() {
        return errors;
    }
}
