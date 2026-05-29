package com.transpiler.parser;

import com.transpiler.ir.ProgramNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for PythonParser MVP.
 */
public class TestPythonParser {
    private PythonParser parser;

    @BeforeEach
    void setUp() {
        parser = new PythonParser();
    }

    @Test
    void testSimpleAssignment() {
        String input = "x = 5";
        ProgramNode program = parser.parse(input);
        assertNotNull(program, "ProgramNode should not be null");
        assertFalse(program.getChildren().isEmpty(), "Program should have at least one child (main block)");
    }

    @Test
    void testPrintCall() {
        String input = "print(x)";
        ProgramNode program = parser.parse(input);
        assertNotNull(program, "ProgramNode should not be null");
    }

    @Test
    void testMultipleStatements() {
        String input = "x = 5\nprint(x)";
        ProgramNode program = parser.parse(input);
        assertNotNull(program, "ProgramNode should not be null");
    }

    @Test
    void testInvalidSyntax() {
        String input = "x = ";
        assertThrows(RuntimeException.class, () -> parser.parse(input), 
            "Invalid syntax should throw RuntimeException");
    }

    @Test
    void testNullInput() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null), 
            "Null input should throw IllegalArgumentException");
    }

    @Test
    void testBlankLinesHandling() {
        // 1. Blank line between statements
        String input1 = "x = 5\n\nprint(x)";
        assertNotNull(parser.parse(input1));

        // 2. Multiple blank lines
        String input2 = "x = 5\n\n\n\nprint(x)";
        assertNotNull(parser.parse(input2));

        // 3. Blank line before loop
        String input3 = "x = 5\n\nfor i in range(1, 5):\n    print(i)";
        assertNotNull(parser.parse(input3));

        // 4. Blank line inside block
        String input4 = "if x > 5:\n    x = 10\n\n    print(x)";
        assertNotNull(parser.parse(input4));

        // 5. Blank line after if/else block
        String input5 = "if x > 5:\n    print(1)\nelse:\n    print(2)\n\nx = 10";
        assertNotNull(parser.parse(input5));
    }
}
