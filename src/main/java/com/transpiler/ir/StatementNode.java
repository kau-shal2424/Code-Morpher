package com.transpiler.ir;

/**
 * Base abstract class for all IR nodes that represent a statement.
 * A statement is an action or a command that can be executed.
 */
public abstract class StatementNode extends IRNode {
    protected StatementNode() {
        super();
    }
}
