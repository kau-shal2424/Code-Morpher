package com.transpiler.generator;

import com.transpiler.ir.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generator for Java.
 */
public class JavaGenerator implements CodeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(JavaGenerator.class);
    private Set<String> declaredVariables;

    @Override
    public String generate(ProgramNode program) {
        if (program == null) {
            throw new IllegalArgumentException("Program node cannot be null");
        }
        logger.info("Generating Java code...");
        declaredVariables = new HashSet<>();
        
        StringBuilder functionsCode = new StringBuilder();
        StringBuilder mainStatementsCode = new StringBuilder();
        
        for (IRNode child : program.getChildren()) {
            if (child instanceof FunctionNode) {
                functionsCode.append(generateNode(child, 1));
            } else {
                mainStatementsCode.append(generateNode(child, 2));
            }
        }
        
        StringBuilder javaCode = new StringBuilder();
        javaCode.append("public class Main {\n");
        javaCode.append(functionsCode);
        javaCode.append("    public static void main(String[] args) {\n");
        javaCode.append(mainStatementsCode);
        javaCode.append("    }\n");
        javaCode.append("}\n");
        return javaCode.toString();
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
        
        logger.warn("Unsupported IR node type for Java generation: {}", node.getClass().getSimpleName());
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
        ExpressionNode value = assignment.getValue();
        String valueStr = generateNode(value, 0);
        
        String prefix = getIndent(indentLevel);
        if (!declaredVariables.contains(target)) {
            declaredVariables.add(target);
            String type = "int"; // Default
            if (value instanceof LiteralNode) {
                LiteralNode lit = (LiteralNode) value;
                if ("string".equals(lit.getType())) {
                    type = "String";
                }
            }
            prefix += type + " ";
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

    private String generateWhileNode(WhileNode whileNode, int indentLevel) {
        String indent = getIndent(indentLevel);
        String condition = generateNode(whileNode.getCondition(), 0);
        
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("while (").append(condition).append(") {\n");
        sb.append(generateNode(whileNode.getBody(), indentLevel + 1));
        sb.append(indent).append("}\n");
        
        return sb.toString();
    }

    private String generateFunctionCall(FunctionCallNode call) {
        String funcName = call.getFunctionName();
        
        // Special case for print -> System.out.println
        if ("print".equals(funcName)) {
            funcName = "System.out.println";
        }
        
        // Special case for len(x) -> x.length()
        if ("len".equals(funcName) && !call.getArguments().isEmpty()) {
            return generateNode(call.getArguments().get(0), 0) + ".length()";
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
        sb.append(indent).append("for (int ").append(varName).append(" = ").append(start).append("; ")
          .append(varName).append(" < ").append(end).append("; ")
          .append(varName).append("++) {\n");
        sb.append(generateNode(forNode.getBody(), indentLevel + 1));
        sb.append(indent).append("}\n");
        
        return sb.toString();
    }

    private String generateFunction(FunctionNode node, int indentLevel) {
        String indent = getIndent(indentLevel);
        String name = node.getName();
        String params = node.getParameters().stream()
                .map(param -> "Object " + param.getName())
                .collect(Collectors.joining(", "));
        
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("// Future enhancement: Java type inference for function parameter types\n");
        sb.append(indent).append("public static Object ").append(name).append("(").append(params).append(") {\n");
        sb.append(generateNode(node.getBody(), indentLevel + 1));
        sb.append(indent).append("}\n");
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
