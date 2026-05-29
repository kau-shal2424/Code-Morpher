package com.transpiler.ir;

/**
 * Represents a return statement.
 */
public class ReturnNode extends StatementNode {
    private final ExpressionNode value;

    public ReturnNode() {
        this(null);
    }

    public ReturnNode(ExpressionNode value) {
        this.value = value;
    }

    public ExpressionNode getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ReturnNode(value=" + value + ")";
    }
}
