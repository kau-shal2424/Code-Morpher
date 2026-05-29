package com.transpiler.generator;

import com.transpiler.ir.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generator for JavaScript.
 */
public class JavaScriptGenerator implements CodeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(JavaScriptGenerator.class);
    private Set<String> declaredVariables;

    @Override
    public String generate(ProgramNode program) {
        if (program == null) {
            throw new IllegalArgumentException("Program node cannot be null");
        }
        logger.info("Generating JavaScript code...");
        declaredVariables = new HashSet<>();
        
        StringBuilder jsCode = new StringBuilder();
        for (IRNode child : program.getChildren()) {
            jsCode.append(generateNode(child, 0));
        }
        return jsCode.toString();
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
        } else if (node instanceof IfNode) {
            return generateIfNode((IfNode) node, indentLevel);
        } else if (node instanceof WhileNode) {
            return generateWhileNode((WhileNode) node, indentLevel);
        } else if (node instanceof ForNode) {
            return generateForNode((ForNode) node, indentLevel);
        } else if (node instanceof VariableRefNode) {
            return generateVariableRef((VariableRefNode) node);
        } else if (node instanceof LiteralNode) {
            return generateLiteral((LiteralNode) node);
        } else if (node instanceof FunctionNode) {
            return generateFunction((FunctionNode) node, indentLevel);
        } else if (node instanceof ReturnNode) {
            return generateReturn((ReturnNode) node, indentLevel);
        }
        
        logger.warn("Unsupported IR node type for JavaScript generation: {}", node.getClass().getSimpleName());
        return getIndent(indentLevel) + "// Unsupported node: " + node.getClass().getSimpleName() + "\n";
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
        String valueStr = generateNode(assignment.getValue(), 0);
        
        String prefix = getIndent(indentLevel);
        if (!declaredVariables.contains(target)) {
            declaredVariables.add(target);
            prefix += "let ";
        }
        
        return prefix + target + " = " + valueStr + ";\n";
    }

    private String generateExpressionStatement(ExpressionStatementNode stmt, int indentLevel) {
        String exprStr = generateNode(stmt.getExpression(), 0);
        return getIndent(indentLevel) + exprStr + ";\n";
    }

    private String generateBinaryOp(BinaryOpNode node) {
        String left = generateNode(node.getLeft(), 0);
        String right = generateNode(node.getRight(), 0);
        return left + " " + node.getOperator() + " " + right;
    }

    private String generateIfNode(IfNode ifNode, int indentLevel) {
        String indent = getIndent(indentLevel);
        String condition = generateNode(ifNode.getCondition(), 0);
        
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("if (").append(condition).append(") {\n");
        sb.append(generateNode(ifNode.getThenBlock(), indentLevel + 1));
        sb.append(indent).append("}");
        
        if (ifNode.getElseBlock() != null) {
            sb.append(" else {\n");
            sb.append(generateNode(ifNode.getElseBlock(), indentLevel + 1));
            sb.append(indent).append("}\n");
        } else {
            sb.append("\n");
        }
        
        return sb.toString();
    }

    private String generateFunctionCall(FunctionCallNode call) {
        String funcName = call.getFunctionName();
        
        // Special case for print -> console.log
        if ("print".equals(funcName)) {
            funcName = "console.log";
        }
        
        // Special case for len(x) -> x.length
        if ("len".equals(funcName) && !call.getArguments().isEmpty()) {
            return generateNode(call.getArguments().get(0), 0) + ".length";
        }
        
        String args = call.getArguments().stream()
                .map(arg -> generateNode(arg, 0))
                .collect(Collectors.joining(", "));
                
        return funcName + "(" + args + ")";
    }

    private String generateVariableRef(VariableRefNode varRef) {
        return varRef.getName();
    }

    private String generateLiteral(LiteralNode literal) {
        Object val = literal.getValue();
        if ("string".equals(literal.getType())) {
            return "\"" + val + "\"";
        }
        return String.valueOf(val);
    }
    
    private String generateForNode(ForNode forNode, int indentLevel) {
        String varName = forNode.getVariableName();
        String start = forNode.getStartExpression() != null ? generateNode(forNode.getStartExpression(), 0) : "0";
        String end = generateNode(forNode.getEndExpression(), 0);
        String indent = getIndent(indentLevel);
        
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("for (let ").append(varName).append(" = ").append(start).append("; ")
          .append(varName).append(" < ").append(end).append("; ")
          .append(varName).append("++) {\n");
        sb.append(generateNode(forNode.getBody(), indentLevel + 1));
        sb.append(indent).append("}\n");
        
        return sb.toString();
    }

    private String generateWhileNode(WhileNode whileNode, int indentLevel) {
        String indent = getIndent(indentLevel);
        String condition = generateNode(whileNode.getCondition(), 0);
        
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("while (").append(condition).append(") {\n");
        sb.append(generateNode(whileNode.getBody(), indentLevel + 1));
        sb.append(indent).append("}\n");
        
        return sb.toString();
    }
    
    private String generateFunction(FunctionNode node, int indentLevel) {
        String indent = getIndent(indentLevel);
        String name = node.getName();
        String params = node.getParameters().stream()
                .map(param -> param.getName())
                .collect(Collectors.joining(", "));
        
        Set<String> previousDeclared = new HashSet<>(declaredVariables);
        for (VariableDeclNode param : node.getParameters()) {
            declaredVariables.add(param.getName());
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("function ").append(name).append("(").append(params).append(") {\n");
        sb.append(generateNode(node.getBody(), indentLevel + 1));
        sb.append(indent).append("}\n");
        
        declaredVariables = previousDeclared;
        return sb.toString();
    }

    private String generateReturn(ReturnNode node, int indentLevel) {
        String indent = getIndent(indentLevel);
        if (node.getValue() == null) {
            return indent + "return;\n";
        }
        String exprStr = generateNode(node.getValue(), 0);
        return indent + "return " + exprStr + ";\n";
    }

    private String getIndent(int level) {
        return "    ".repeat(Math.max(0, level));
    }
}
