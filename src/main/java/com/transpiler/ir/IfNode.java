package com.transpiler.ir;

/**
 * Represents an if statement in the IR.
 */
public class IfNode extends StatementNode {
    private final ExpressionNode condition;
    private final BlockNode thenBlock;
    private final BlockNode elseBlock;

    public IfNode(ExpressionNode condition, BlockNode thenBlock) {
        this(condition, thenBlock, null);
    }

    public IfNode(ExpressionNode condition, BlockNode thenBlock, BlockNode elseBlock) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public BlockNode getThenBlock() {
        return thenBlock;
    }

    public BlockNode getElseBlock() {
        return elseBlock;
    }

    @Override
    public String toString() {
        return "IfNode(condition=" + condition + ", then=" + thenBlock + ", else=" + elseBlock + ")";
    }
}
