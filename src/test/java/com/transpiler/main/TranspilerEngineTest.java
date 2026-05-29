package com.transpiler.main;

import com.transpiler.parser.ParserRegistry;
import com.transpiler.generator.GeneratorRegistry;
import com.transpiler.utils.Language;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TranspilerEngine and registries.
 */
public class TranspilerEngineTest {

    @Test
    void testEngineInitialization() {
        TranspilerEngine engine = new TranspilerEngine();
        assertNotNull(engine);
    }

    @Test
    void testParserRegistry() {
        assertNotNull(ParserRegistry.getParser(Language.PYTHON));
        assertNotNull(ParserRegistry.getParser(Language.JAVA));
    }

    @Test
    void testGeneratorRegistry() {
        assertNotNull(GeneratorRegistry.getGenerator(Language.PYTHON));
        assertNotNull(GeneratorRegistry.getGenerator(Language.JAVA));
    }

    @Test
    void testLanguageEnum() {
        assertEquals(Language.PYTHON, Language.fromString("python").get());
        assertEquals(Language.JAVA, Language.fromString("Java").get());
        assertThrows(IllegalArgumentException.class, () -> Language.validate("invalid"));
    }

    @Test
    void testPythonToJavaScriptTranspilation() {
        TranspilerEngine engine = new TranspilerEngine();
        String pythonCode = "x = 5\nprint(x)\n";
        String jsCode = engine.transpile(pythonCode, Language.PYTHON, Language.JAVASCRIPT);
        
        String expectedJs = "let x = 5;\nconsole.log(x);\n";
        assertEquals(expectedJs, jsCode);
    }
    
    @Test
    void testPythonToJavaTranspilation() {
        TranspilerEngine engine = new TranspilerEngine();
        String pythonCode = "x = 5\nprint(x)\n";
        String javaCode = engine.transpile(pythonCode, Language.PYTHON, Language.JAVA);
        
        String expectedJava = "public class Main {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        int x = 5;\n" +
                              "        System.out.println(x);\n" +
                              "    }\n" +
                              "}\n";
        assertEquals(expectedJava, javaCode);
    }
    
    @Test
    void testPythonToCppTranspilation() {
        TranspilerEngine engine = new TranspilerEngine();
        String pythonCode = "x = 5\nprint(x)\n";
        String cppCode = engine.transpile(pythonCode, Language.PYTHON, Language.CPP);
        
        String expectedCpp = "#include <iostream>\n" +
                             "using namespace std;\n\n" +
                             "int main() {\n" +
                             "    int x = 5;\n" +
                             "    cout << x << endl;\n" +
                             "    return 0;\n" +
                             "}\n";
        assertEquals(expectedCpp, cppCode);
    }

    @Test
    void testBinaryExpressionTranspilation() {
        TranspilerEngine engine = new TranspilerEngine();
        String pythonCode = "x = 5 + 3\n";
        String jsCode = engine.transpile(pythonCode, Language.PYTHON, Language.JAVASCRIPT);
        assertEquals("let x = 5 + 3;\n", jsCode);
    }

    @Test
    void testIfStatementTranspilation() {
        TranspilerEngine engine = new TranspilerEngine();
        String pythonCode = "x = 5\n" +
                           "if x > 3:\n" +
                           "    print(x)\n";
        String jsCode = engine.transpile(pythonCode, Language.PYTHON, Language.JAVASCRIPT);

        String expected = "let x = 5;\n" +
                          "if (x > 3) {\n" +
                          "    console.log(x);\n" +
                          "}\n";
        assertEquals(expected, jsCode);
    }

    @Test
    void testMultiLineIfTranspilation() {
        TranspilerEngine engine = new TranspilerEngine();
        String pythonCode = "x = 5\n" +
                           "if x > 3:\n" +
                           "    print(x)\n" +
                           "    print(10)\n";
        String jsCode = engine.transpile(pythonCode, Language.PYTHON, Language.JAVASCRIPT);

        String expected = "let x = 5;\n" +
                          "if (x > 3) {\n" +
                          "    console.log(x);\n" +
                          "    console.log(10);\n" +
                          "}\n";
        assertEquals(expected, jsCode);
    }

    @Test
    void testForRangeLoopTranspilation() {
        TranspilerEngine engine = new TranspilerEngine();
        String pythonCode = "x = 5\n" +
                           "for i in range(x):\n" +
                           "    print(i)\n";
        String jsCode = engine.transpile(pythonCode, Language.PYTHON, Language.JAVASCRIPT);

        String expected = "let x = 5;\n" +
                          "for (let i = 0; i < x; i++) {\n" +
                          "    console.log(i);\n" +
                          "}\n";
        assertEquals(expected, jsCode);
    }

    @Test
    void testStringAndLenTranspilation() {
        TranspilerEngine engine = new TranspilerEngine();
        String pythonCode = "x = \"asmit\"\n" +
                           "for i in range(len(x)):\n" +
                           "    print(x)\n";
        
        // JavaScript Verification
        String jsCode = engine.transpile(pythonCode, Language.PYTHON, Language.JAVASCRIPT);
        String expectedJs = "let x = \"asmit\";\n" +
                            "for (let i = 0; i < x.length; i++) {\n" +
                            "    console.log(x);\n" +
                            "}\n";
        assertEquals(expectedJs, jsCode);

        // Java Verification
        String javaCode = engine.transpile(pythonCode, Language.PYTHON, Language.JAVA);
        String expectedJava = "public class Main {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        String x = \"asmit\";\n" +
                              "        for (int i = 0; i < x.length(); i++) {\n" +
                              "            System.out.println(x);\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n";
        assertEquals(expectedJava, javaCode);

        // C++ Verification
        String cppCode = engine.transpile(pythonCode, Language.PYTHON, Language.CPP);
        String expectedCpp = "#include <iostream>\n" +
                             "#include <string>\n" +
                             "using namespace std;\n\n" +
                             "int main() {\n" +
                             "    string x = \"asmit\";\n" +
                             "    for (int i = 0; i < x.length(); i++) {\n" +
                             "        cout << x << endl;\n" +
                             "    }\n" +
                             "    return 0;\n" +
                             "}\n";
        assertEquals(expectedCpp, cppCode);
    }

    @Test
    void testPrintLiteral() {
        TranspilerEngine engine = new TranspilerEngine();
        String pythonCode = "print(\"hello\")\n";
        
        assertEquals("console.log(\"hello\");\n", engine.transpile(pythonCode, Language.PYTHON, Language.JAVASCRIPT));
        assertTrue(engine.transpile(pythonCode, Language.PYTHON, Language.JAVA).contains("System.out.println(\"hello\");"));
        assertTrue(engine.transpile(pythonCode, Language.PYTHON, Language.CPP).contains("cout << \"hello\" << endl"));
    }

    @Test
    void testElseTranspilation() {
        TranspilerEngine engine = new TranspilerEngine();
        String pythonCode = "x = 5\n" +
                           "if x > 3:\n" +
                           "    print(x)\n" +
                           "else:\n" +
                           "    print(\"small\")\n";
                           
        String jsCode = engine.transpile(pythonCode, Language.PYTHON, Language.JAVASCRIPT);
        assertTrue(jsCode.contains("} else {"));
        assertTrue(jsCode.contains("console.log(\"small\")"));
    }

    @Test
    void testWhileLoopTranspilation() {
        TranspilerEngine engine = new TranspilerEngine();
        String pythonCode = "i = 0\n" +
                           "while i < 5:\n" +
                           "    print(i)\n" +
                           "    i = i + 1\n";
                           
        String jsCode = engine.transpile(pythonCode, Language.PYTHON, Language.JAVASCRIPT);
        assertTrue(jsCode.contains("while (i < 5) {"));
        assertTrue(jsCode.contains("i = i + 1;"));
    }

    @Test
    void testRangeStartEndLoop() {
        TranspilerEngine engine = new TranspilerEngine();
        String pythonCode = "for i in range(1, 5):\n" +
                           "    print(i)\n";
                           
        String jsCode = engine.transpile(pythonCode, Language.PYTHON, Language.JAVASCRIPT);
        assertTrue(jsCode.contains("for (let i = 1; i < 5; i++)"));
        
        String cppCode = engine.transpile(pythonCode, Language.PYTHON, Language.CPP);
        assertTrue(cppCode.contains("for (int i = 1; i < 5; i++)"));
    }
}
