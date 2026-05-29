package com.transpiler.ir;

/**
 * Represents a literal value (int, string, bool, etc.) in the IR.
 */
public class LiteralNode extends ExpressionNode {
    private final Object value;
    private final String type;

    public LiteralNode(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "LiteralNode(value=" + value + ", type='" + type + "')";
    }
}
