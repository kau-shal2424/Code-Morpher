package com.transpiler.ir;

/**
 * Represents a while loop.
 */
public class WhileNode extends StatementNode {
    private final ExpressionNode condition;
    private final BlockNode body;

    public WhileNode(ExpressionNode condition, BlockNode body) {
        this.condition = condition;
        this.body = body;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public BlockNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "WhileNode(condition=" + condition + ", body=" + body + ")";
    }
}
