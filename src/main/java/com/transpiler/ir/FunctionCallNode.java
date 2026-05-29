package com.transpiler.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a function call in an expression.
 */
public class FunctionCallNode extends ExpressionNode {
    private final String functionName;
    private final List<ExpressionNode> arguments;

    public FunctionCallNode(String functionName) {
        this.functionName = functionName;
        this.arguments = new ArrayList<>();
    }

    public void addArgument(ExpressionNode argument) {
        this.arguments.add(argument);
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<ExpressionNode> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "FunctionCallNode(name='" + functionName + "', args=" + arguments + ")";
    }
}
