package com.transpiler.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a function definition in the IR.
 */
public class FunctionNode extends IRNode {
    private final String name;
    private final String returnType;
    private final List<VariableDeclNode> parameters;
    private final BlockNode body;

    public FunctionNode(String name, String returnType, BlockNode body) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
        this.body = body;
    }

    public void addParameter(VariableDeclNode parameter) {
        this.parameters.add(parameter);
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<VariableDeclNode> getParameters() {
        return parameters;
    }

    public BlockNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "FunctionNode(name='" + name + "', returnType='" + returnType + "', params=" + parameters + ", body=" + body + ")";
    }
}
