package com.transpiler.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a complete program, consisting of list of functions and statements.
 */
public class ProgramNode extends IRNode {
    private final List<IRNode> children;
    private final List<com.transpiler.dto.CompilerErrorDto> errors = new java.util.ArrayList<>();

    public ProgramNode() {
        this.children = new ArrayList<>();
    }

    public List<com.transpiler.dto.CompilerErrorDto> getErrors() {
        return errors;
    }

    public void addError(com.transpiler.dto.CompilerErrorDto error) {
        this.errors.add(error);
    }

    private com.transpiler.dto.ParseTreeNodeDto cst;

    public com.transpiler.dto.ParseTreeNodeDto getCst() {
        return cst;
    }

    public void setCst(com.transpiler.dto.ParseTreeNodeDto cst) {
        this.cst = cst;
    }

    public void addChild(IRNode child) {
        this.children.add(child);
    }

    public List<IRNode> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "ProgramNode(\n" +
                children.stream()
                        .map(child -> "  " + child.toString().replace("\n", "\n  "))
                        .collect(Collectors.joining("\n")) +
                "\n)";
    }
}
