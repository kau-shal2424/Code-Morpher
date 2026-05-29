package com.transpiler.ir;

/**
 * Represents a binary operation (e.g., +, -, *, /) in the IR.
 */
public class BinaryOpNode extends ExpressionNode {
    private final ExpressionNode left;
    private final String operator;
    private final ExpressionNode right;

    public BinaryOpNode(ExpressionNode left, String operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public String getOperator() {
        return operator;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "BinaryOpNode(left=" + left + ", op='" + operator + "', right=" + right + ")";
    }
}
