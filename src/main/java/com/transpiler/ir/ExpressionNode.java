package com.transpiler.ir;

/**
 * Base abstract class for all IR nodes that represent an expression.
 * An expression is a combination of literals, variables, and operators that evaluate to a value.
 */
public abstract class ExpressionNode extends IRNode {
    protected ExpressionNode() {
        super();
    }
}
