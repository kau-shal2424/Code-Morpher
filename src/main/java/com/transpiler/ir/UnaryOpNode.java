package com.transpiler.ir;

/**
 * Represents a unary operation (e.g., -, !, ++) in the IR.
 */
public class UnaryOpNode extends ExpressionNode {
    private final String operator;
    private final ExpressionNode operand;
    private final boolean isPrefix;

    public UnaryOpNode(String operator, ExpressionNode operand, boolean isPrefix) {
        this.operator = operator;
        this.operand = operand;
        this.isPrefix = isPrefix;
    }

    public String getOperator() {
        return operator;
    }

    public ExpressionNode getOperand() {
        return operand;
    }

    public boolean isPrefix() {
        return isPrefix;
    }

    @Override
    public String toString() {
        return "UnaryOpNode(op='" + operator + "', operand=" + operand + ", isPrefix=" + isPrefix + ")";
    }
}
