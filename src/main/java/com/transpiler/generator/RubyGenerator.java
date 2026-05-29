package com.transpiler.generator;

import com.transpiler.ir.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generator for Ruby.
 */
public class RubyGenerator implements CodeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RubyGenerator.class);
    private Set<String> declaredVariables;

    @Override
    public String generate(ProgramNode program) {
        if (program == null) {
            throw new IllegalArgumentException("Program node cannot be null");
        }
        logger.info("Generating Ruby code...");
        declaredVariables = new HashSet<>();
        
        StringBuilder rubyCode = new StringBuilder();
        for (IRNode child : program.getChildren()) {
            rubyCode.append(generateNode(child, 0));
        }
        
        return rubyCode.toString();
    }
    
    private String generateNode(IRNode node, int indentLevel) {
        if (node instanceof BlockNode) {
            return generateBlock((BlockNode) node, indentLevel);
        } else if (node instanceof AssignmentNode) {
            return generateAssignment((AssignmentNode) node, indentLevel);
        } else if (node instanceof ExpressionStatementNode) {
            return generateExpressionStatement((ExpressionStatementNode) node, indentLevel);
        } else if (node instanceof FunctionCallNode) {
            return generateFunctionCall((FunctionCallNode) node);
        } else if (node instanceof BinaryOpNode) {
            return generateBinaryOp((BinaryOpNode) node);
        } else if (node instanceof LiteralNode) {
            return generateLiteral((LiteralNode) node);
        } else if (node instanceof VariableRefNode) {
            return generateVariableRef((VariableRefNode) node);
        } else if (node instanceof FunctionNode) {
            return generateFunction((FunctionNode) node, indentLevel);
        } else if (node instanceof ReturnNode) {
            return generateReturn((ReturnNode) node, indentLevel);
        }
        
        logger.warn("Unsupported IR node for Ruby: {}", node.getClass().getSimpleName());
        return "";
    }

    private String generateBlock(BlockNode block, int indentLevel) {
        StringBuilder sb = new StringBuilder();
        for (StatementNode stmt : block.getStatements()) {
            sb.append(generateNode(stmt, indentLevel));
        }
        return sb.toString();
    }

    private String generateAssignment(AssignmentNode assignment, int indentLevel) {
        String target = assignment.getTarget();
        ExpressionNode value = assignment.getValue();
        String valueStr = generateNode(value, 0);
        
        // Ruby does not use keywords like let or var
        declaredVariables.add(target);
        
        return getIndent(indentLevel) + target + " = " + valueStr + "\n";
    }

    private String generateExpressionStatement(ExpressionStatementNode stmt, int indentLevel) {
        String exprStr = generateNode(stmt.getExpression(), 0);
        return getIndent(indentLevel) + exprStr + "\n";
    }

    private String generateFunctionCall(FunctionCallNode call) {
        String funcName = call.getFunctionName();
        
        if ("print".equals(funcName)) {
            funcName = "puts";
        }
        
        String args = call.getArguments().stream()
                .map(arg -> generateNode(arg, 0))
                .collect(Collectors.joining(", "));
        return funcName + " " + args;
    }

    private String generateBinaryOp(BinaryOpNode node) {
        return generateNode(node.getLeft(), 0) + " " + node.getOperator() + " " + generateNode(node.getRight(), 0);
    }

    private String generateLiteral(LiteralNode literal) {
        Object val = literal.getValue();
        if ("string".equals(literal.getType())) {
            return "\"" + val + "\"";
        }
        return String.valueOf(val);
    }

    private String generateVariableRef(VariableRefNode varRef) {
        return varRef.getName();
    }

    private String generateFunction(FunctionNode node, int indentLevel) {
        String indent = getIndent(indentLevel);
        String name = node.getName();
        String params = node.getParameters().stream()
                .map(param -> param.getName())
                .collect(Collectors.joining(", "));
        
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("def ").append(name);
        if (!params.isEmpty()) {
            sb.append("(").append(params).append(")");
        }
        sb.append("\n");
        sb.append(generateNode(node.getBody(), indentLevel + 1));
        sb.append(indent).append("end\n\n");
        return sb.toString();
    }

    private String generateReturn(ReturnNode node, int indentLevel) {
        String indent = getIndent(indentLevel);
        if (node.getValue() == null) {
            return indent + "return\n";
        }
        String exprStr = generateNode(node.getValue(), 0);
        return indent + "return " + exprStr + "\n";
    }

    private String getIndent(int level) {
        return "  ".repeat(level);
    }
}
