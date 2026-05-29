package com.transpiler.ir;

/**
 * Represents a for loop: for variableName in range(limitExpression)
 */
public class ForNode extends StatementNode {
    private final String variableName;
    private final ExpressionNode startExpression;
    private final ExpressionNode endExpression;
    private final BlockNode body;

    public ForNode(String variableName, ExpressionNode endExpression, BlockNode body) {
        this(variableName, null, endExpression, body);
    }

    public ForNode(String variableName, ExpressionNode startExpression, ExpressionNode endExpression, BlockNode body) {
        this.variableName = variableName;
        this.startExpression = startExpression;
        this.endExpression = endExpression;
        this.body = body;
    }

    public String getVariableName() {
        return variableName;
    }

    public ExpressionNode getStartExpression() {
        return startExpression;
    }

    public ExpressionNode getEndExpression() {
        return endExpression;
    }

    public BlockNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "ForNode(var=" + variableName + ", start=" + startExpression + ", end=" + endExpression + ", body=" + body + ")";
    }
}
