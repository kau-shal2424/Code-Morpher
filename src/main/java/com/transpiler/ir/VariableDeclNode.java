package com.transpiler.ir;

/**
 * Represents a variable declaration in the IR.
 */
public class VariableDeclNode extends StatementNode {
    private final String type;
    private final String name;
    private final ExpressionNode initializer;

    public VariableDeclNode(String type, String name) {
        this(type, name, null);
    }

    public VariableDeclNode(String type, String name, ExpressionNode initializer) {
        this.type = type;
        this.name = name;
        this.initializer = initializer;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ExpressionNode getInitializer() {
        return initializer;
    }

    @Override
    public String toString() {
        return "VariableDeclNode(type='" + type + "', name='" + name + "', initializer=" + initializer + ")";
    }
}
