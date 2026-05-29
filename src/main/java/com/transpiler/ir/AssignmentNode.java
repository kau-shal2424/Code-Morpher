package com.transpiler.ir;

/**
 * Represents an assignment statement in the IR.
 */
public class AssignmentNode extends StatementNode {
    private final String target;
    private final ExpressionNode value;

    public AssignmentNode(String target, ExpressionNode value) {
        this.target = target;
        this.value = value;
    }

    public String getTarget() {
        return target;
    }

    public ExpressionNode getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "AssignmentNode(target='" + target + "', value=" + value + ")";
    }
}
