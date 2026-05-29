package com.transpiler.generator;

import com.transpiler.ir.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generator for TypeScript.
 */
public class TypeScriptGenerator implements CodeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TypeScriptGenerator.class);
    private Set<String> declaredVariables;
    private Map<String, Set<String>> variableAllTypes;

    @Override
    public String generate(ProgramNode program) {
        if (program == null) {
            throw new IllegalArgumentException("Program node cannot be null");
        }
        logger.info("Generating TypeScript code...");
        declaredVariables = new HashSet<>();
        variableAllTypes = new HashMap<>();
        
        // Phase 1: Pre-pass to find all types for each variable
        analyzeTypes(program);
        
        // Phase 2: Generation
        StringBuilder tsCode = new StringBuilder();
        for (IRNode child : program.getChildren()) {
            tsCode.append(generateNode(child, 0));
        }
        
        return tsCode.toString();
    }
    
    private void analyzeTypes(IRNode node) {
        if (node instanceof ProgramNode) {
            for (IRNode child : ((ProgramNode) node).getChildren()) {
                analyzeTypes(child);
            }
        } else if (node instanceof BlockNode) {
            for (StatementNode stmt : ((BlockNode) node).getStatements()) {
                analyzeTypes(stmt);
            }
        } else if (node instanceof AssignmentNode) {
            AssignmentNode assign = (AssignmentNode) node;
            String type = inferType(assign.getValue());
            variableAllTypes.computeIfAbsent(assign.getTarget(), k -> new TreeSet<>()).add(type);
        } else if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;
            analyzeTypes(ifNode.getThenBlock());
            if (ifNode.getElseBlock() != null) analyzeTypes(ifNode.getElseBlock());
        } else if (node instanceof WhileNode) {
            analyzeTypes(((WhileNode) node).getBody());
        } else if (node instanceof ForNode) {
            ForNode forNode = (ForNode) node;
            variableAllTypes.computeIfAbsent(forNode.getVariableName(), k -> new HashSet<>()).add("int");
            analyzeTypes(forNode.getBody());
        } else if (node instanceof FunctionNode) {
            analyzeTypes(((FunctionNode) node).getBody());
        }
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
        
        logger.warn("Unsupported IR node for TypeScript: {}", node.getClass().getSimpleName());
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
        
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indentLevel));
        
        if (!declaredVariables.contains(target)) {
            declaredVariables.add(target);
            Set<String> types = variableAllTypes.get(target);
            String tsType = types.stream()
                    .map(this::mapType)
                    .distinct()
                    .collect(Collectors.joining(" | "));
            sb.append("let ").append(target).append(": ").append(tsType).append(" = ").append(valueStr).append(";\n");
        } else {
            sb.append(target).append(" = ").append(valueStr).append(";\n");
        }
        
        return sb.toString();
    }

    private String generateExpressionStatement(ExpressionStatementNode stmt, int indentLevel) {
        String exprStr = generateNode(stmt.getExpression(), 0);
        return getIndent(indentLevel) + exprStr + ";\n";
    }

    private String generateFunctionCall(FunctionCallNode call) {
        String funcName = call.getFunctionName();
        
        if ("print".equals(funcName)) {
            funcName = "console.log";
        }
        
        String args = call.getArguments().stream()
                .map(arg -> generateNode(arg, 0))
                .collect(Collectors.joining(", "));
        return funcName + "(" + args + ")";
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

    private String inferType(ExpressionNode expr) {
        if (expr instanceof LiteralNode) {
            return ((LiteralNode) expr).getType();
        } else if (expr instanceof VariableRefNode) {
            // In TypeScript, we use the pre-calculated union type, but here we just need a single type for mapping
            return "any"; 
        } else if (expr instanceof BinaryOpNode) {
            return "number";
        }
        return "any";
    }

    private String mapType(String type) {
        switch (type) {
            case "int":
            case "float": return "number";
            case "string": return "string";
            case "boolean": return "boolean";
            default: return "any";
        }
    }

    private String generateFunction(FunctionNode node, int indentLevel) {
        String indent = getIndent(indentLevel);
        String name = node.getName();
        String params = node.getParameters().stream()
                .map(param -> param.getName() + ": any") // Dynamic types default to any in TypeScript
                .collect(Collectors.joining(", "));
        
        Set<String> previousDeclared = new HashSet<>(declaredVariables);
        for (VariableDeclNode param : node.getParameters()) {
            declaredVariables.add(param.getName());
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("function ").append(name).append("(").append(params).append("): any {\n");
        sb.append(generateNode(node.getBody(), indentLevel + 1));
        sb.append(indent).append("}\n\n");
        
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
        return "    ".repeat(level);
    }
}
