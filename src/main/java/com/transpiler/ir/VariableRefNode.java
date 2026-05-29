package com.transpiler.ir;

/**
 * Represents a reference to a variable in an expression.
 */
public class VariableRefNode extends ExpressionNode {
    private final String name;

    public VariableRefNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "VariableRefNode(name='" + name + "')";
    }
}
