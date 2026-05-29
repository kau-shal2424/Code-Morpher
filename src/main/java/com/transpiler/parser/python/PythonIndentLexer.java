package com.transpiler.parser.python;

import com.transpiler.grammar.python.Python3Lexer;
import org.antlr.v4.runtime.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Custom ANTLR lexer that wraps Python3Lexer and injects synthetic INDENT and DEDENT tokens
 * based on Python's indentation rules.
 *
 * <p>After each NEWLINE token, the lexer inspects the indentation of the next line.
 * If indentation increases it emits an INDENT token. If it decreases it emits one or more
 * DEDENT tokens until the indentation stack is restored.
 */
public class PythonIndentLexer extends Python3Lexer {

    /** Queue of pending synthetic tokens to emit before resuming normal lexing. */
    private final List<Token> pendingTokens = new ArrayList<>();

    /** Stack of indentation levels (in spaces). Level 0 is always at the bottom. */
    private final Deque<Integer> indentStack = new ArrayDeque<>();

    /** Whether we just saw a NEWLINE and are waiting to check indentation. */
    private boolean afterNewline = false;

    public PythonIndentLexer(CharStream input) {
        super(input);
        indentStack.push(0);  // base level
    }

    @Override
    public Token nextToken() {
        // Drain any tokens we have queued up first
        if (!pendingTokens.isEmpty()) {
            return pendingTokens.remove(0);
        }

        Token token = super.nextToken();

        // Skip consecutive NEWLINE tokens (blank lines) if we just processed a NEWLINE
        while (token.getType() == Python3Lexer.NEWLINE && afterNewline) {
            token = super.nextToken();
        }

        if (token.getType() == Python3Lexer.NEWLINE) {
            // Emit the NEWLINE itself, then check indentation on the next token
            afterNewline = true;
            return token;
        }

        if (afterNewline && token.getType() != Token.EOF) {
            afterNewline = false;

            // Measure indentation of the current token from its column position
            int currentIndent = token.getCharPositionInLine();

            int previousIndent = indentStack.peek();

            if (currentIndent > previousIndent) {
                // Deeper indentation → INDENT
                indentStack.push(currentIndent);
                pendingTokens.add(token); // emit the real token after INDENT
                return makeIndentToken(Python3Lexer.INDENT, token);
            } else if (currentIndent < previousIndent) {
                // Shallower indentation → one or more DEDENTs
                while (indentStack.size() > 1 && indentStack.peek() > currentIndent) {
                    indentStack.pop();
                    pendingTokens.add(makeIndentToken(Python3Lexer.DEDENT, token));
                }
                pendingTokens.add(token); // emit the real token after DEDENT(s)
                return pendingTokens.remove(0);
            }
            // Same indentation — fall through and return the token normally
        }

        if (token.getType() == Token.EOF) {
            afterNewline = false;
            // Emit any remaining DEDENTs before EOF
            while (indentStack.size() > 1) {
                indentStack.pop();
                pendingTokens.add(makeIndentToken(Python3Lexer.DEDENT, token));
            }
            pendingTokens.add(token);
            return pendingTokens.remove(0);
        }

        return token;
    }

    private CommonToken makeIndentToken(int type, Token ref) {
        CommonToken t = new CommonToken(ref);
        t.setType(type);
        t.setText(type == Python3Lexer.INDENT ? "<INDENT>" : "<DEDENT>");
        return t;
    }
}
