package com.transpiler.parser;

import com.transpiler.grammar.python.Python3Parser;
import com.transpiler.ir.ProgramNode;
import com.transpiler.parser.python.PythonIndentLexer;
import com.transpiler.parser.python.PythonToIrVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Professional Python parser implementation using ANTLR4.
 */
public class PythonParser implements LanguageParser {
    private static final Logger logger = LoggerFactory.getLogger(PythonParser.class);

    @Override
    public ProgramNode parse(final String sourceCode) {
        if (sourceCode == null) {
            throw new IllegalArgumentException("Source code cannot be null");
        }

        logger.info("Starting Python parsing MVP...");

        try {
            // 1. Create Lexer (PythonIndentLexer handles INDENT/DEDENT injection)
            final PythonIndentLexer lexer = new PythonIndentLexer(CharStreams.fromString(sourceCode));
            
            // 2. Create Token Stream
            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            
            // 3. Create Parser
            final Python3Parser parser = new Python3Parser(tokens);
            
            // 4. Add Custom Error Listener to capture syntax errors
            final CompilerErrorListener errorListener = new CompilerErrorListener();
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
            
            // 5. Parse using file_input rule
            logger.debug("Running ANTLR parser for file_input...");
            final ParseTree tree = parser.file_input();
            
            // 6. Invoke Visitor to build IR
            logger.debug("Invoking PythonToIrVisitor...");
            final PythonToIrVisitor visitor = new PythonToIrVisitor();
            final ProgramNode program = (ProgramNode) visitor.visit(tree);
            
            if (program != null) {
                // 7. Store captured syntax errors in the ProgramNode for downstream diagnostics
                for (com.transpiler.dto.CompilerErrorDto err : errorListener.getErrors()) {
                    program.addError(err);
                }
                // 8. Serialize and compact Concrete Syntax Tree (CST)
                try {
                    com.transpiler.dto.ParseTreeNodeDto cstNode = new com.transpiler.visitor.CSTSerializationVisitor().serialize(tree, parser);
                    program.setCst(cstNode);
                } catch (Exception e) {
                    logger.warn("Failed to serialize compacted CST: {}", e.getMessage());
                }
            }
            
            logger.info("Python parsing successful");
            return program;

        } catch (Exception e) {
            logger.error("Python parsing failed: {}", e.getMessage());
            throw new RuntimeException("Python parsing failed", e);
        }
    }
}
