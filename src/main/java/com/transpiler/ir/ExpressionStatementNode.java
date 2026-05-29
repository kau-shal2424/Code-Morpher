package com.transpiler.ir;

/**
 * Represents a statement that consists of a single expression.
 */
public class ExpressionStatementNode extends StatementNode {
    private final ExpressionNode expression;

    public ExpressionStatementNode(ExpressionNode expression) {
        this.expression = expression;
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return "ExpressionStatementNode(expression=" + expression + ")";
    }
}
