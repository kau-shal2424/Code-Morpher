package com.transpiler.main;

import com.transpiler.utils.Language;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MultiTargetTranspilerTest {

    private final TranspilerEngine engine = new TranspilerEngine();

    @Test
    void testPythonToC() {
        String input = "x = 5\nx = \"hello\"\nprint(x)";
        String result = engine.transpile(input, Language.PYTHON, Language.C);
        
        assertTrue(result.contains("#include <stdio.h>"), "C output should contain stdio.h");
        assertTrue(result.contains("void* x = 5;"), "C output should use void* for type-changing variable");
        assertTrue(result.contains("x = \"hello\";"), "C output should assign string to x");
        assertTrue(result.contains("printf(\"%s\\n\", x)"), "C output should use printf with %s");
    }

    @Test
    void testPythonToTypeScript() {
        String input = "x = 5\nx = \"hello\"\nprint(x)";
        String result = engine.transpile(input, Language.PYTHON, Language.TYPESCRIPT);
        
        assertTrue(result.contains("let x: number | string = 5;"), "TS output should use union type");
        assertTrue(result.contains("x = \"hello\";"), "TS output should assign string to x");
        assertTrue(result.contains("console.log(x)"), "TS output should use console.log");
    }

    @Test
    void testPythonToRuby() {
        String input = "x = 5\nx = \"hello\"\nprint(x)";
        String result = engine.transpile(input, Language.PYTHON, Language.RUBY);
        
        assertFalse(result.contains("let "), "Ruby output should not have let");
        assertTrue(result.contains("x = 5"), "Ruby output should assign 5 to x");
        assertTrue(result.contains("x = \"hello\""), "Ruby output should assign string to x");
        assertTrue(result.contains("puts x"), "Ruby output should use puts");
    }
}
