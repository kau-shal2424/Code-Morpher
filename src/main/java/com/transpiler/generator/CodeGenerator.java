package com.transpiler.generator;

import com.transpiler.ir.ProgramNode;

/**
 * Interface for language-specific code generators.
 */
public interface CodeGenerator {
    /**
     * Generates source code from a common IR representation.
     *
     * @param program The ProgramNode representing the IR.
     * @return The generated source code as a string.
     */
    String generate(ProgramNode program);
}
