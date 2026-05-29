package com.transpiler.generator;

import com.transpiler.ir.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generator for C.
 */
public class CGenerator implements CodeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(CGenerator.class);
    private Set<String> declaredVariables;
    private Map<String, String> variableTypes;
    private Map<String, Set<String>> variableAllTypes;

    @Override
    public String generate(ProgramNode program) {
        if (program == null) {
            throw new IllegalArgumentException("Program node cannot be null");
        }
        logger.info("Generating C code...");
        declaredVariables = new HashSet<>();
        variableTypes = new HashMap<>();
        variableAllTypes = new HashMap<>();

        // Phase 1: Pre-pass to find variables that change type
        analyzeTypes(program);
        
        StringBuilder functionsCode = new StringBuilder();
        StringBuilder mainStatementsCode = new StringBuilder();
        
        for (IRNode child : program.getChildren()) {
            if (child instanceof FunctionNode) {
                functionsCode.append(generateNode(child, 0));
            } else {
                mainStatementsCode.append(generateNode(child, 1));
            }
        }
        
        StringBuilder cCode = new StringBuilder();
        cCode.append("#include <stdio.h>\n");
        cCode.append("#include <stdbool.h>\n\n");
        cCode.append(functionsCode);
        cCode.append("int main() {\n");
        cCode.append(mainStatementsCode);
        cCode.append("    return 0;\n");
        cCode.append("}\n");
        return cCode.toString();
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
        
        logger.warn("Unsupported IR node for C: {}", node.getClass().getSimpleName());
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
        String currentType = inferType(value);
        
        StringBuilder sb = new StringBuilder();
        sb.append(getIndent(indentLevel));
        
        if (!declaredVariables.contains(target)) {
            declaredVariables.add(target);
            Set<String> allTypes = variableAllTypes.getOrDefault(target, Collections.singleton(currentType));
            String finalType;
            if (allTypes.size() > 1) {
                logger.warn("Type change detected for variable '{}': {}. Falling back to void*.", target, allTypes);
                finalType = "void*";
            } else {
                finalType = mapType(currentType);
            }
            variableTypes.put(target, currentType); // Keep track of current type for printf
            sb.append(finalType).append(" ");
        } else {
            String existingType = variableTypes.get(target);
            if (!existingType.equals(currentType)) {
                logger.warn("Type change detected for variable '{}': {} -> {}.", target, existingType, currentType);
                variableTypes.put(target, currentType); // Update current type for subsequent nodes
            }
        }
        
        sb.append(target).append(" = ").append(valueStr).append(";\n");
        return sb.toString();
    }

    private String generateExpressionStatement(ExpressionStatementNode stmt, int indentLevel) {
        String exprStr = generateNode(stmt.getExpression(), 0);
        return getIndent(indentLevel) + exprStr + ";\n";
    }

    private String generateFunctionCall(FunctionCallNode call) {
        String funcName = call.getFunctionName();
        
        if ("print".equals(funcName)) {
            return generatePrintf(call);
        }
        
        String args = call.getArguments().stream()
                .map(arg -> generateNode(arg, 0))
                .collect(Collectors.joining(", "));
        return funcName + "(" + args + ")";
    }

    private String generatePrintf(FunctionCallNode call) {
        if (call.getArguments().isEmpty()) {
            return "printf(\"\\n\")";
        }
        
        StringBuilder format = new StringBuilder();
        List<String> args = new ArrayList<>();
        
        for (ExpressionNode arg : call.getArguments()) {
            String type = inferType(arg);
            switch (type) {
                case "int": format.append("%d"); break;
                case "float": format.append("%f"); break;
                case "string": format.append("%s"); break;
                case "boolean": format.append("%d"); break; // C uses int for bool
                default: format.append("%s"); break;
            }
            args.add(generateNode(arg, 0));
        }
        format.append("\\n");
        
        return "printf(\"" + format.toString() + "\", " + String.join(", ", args) + ")";
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
            return variableTypes.getOrDefault(((VariableRefNode) expr).getName(), "int");
        } else if (expr instanceof BinaryOpNode) {
            // Simplistic inference: if either is float, result is float
            String leftType = inferType(((BinaryOpNode) expr).getLeft());
            String rightType = inferType(((BinaryOpNode) expr).getRight());
            if ("float".equals(leftType) || "float".equals(rightType)) return "float";
            if ("string".equals(leftType) || "string".equals(rightType)) return "string";
            return "int";
        }
        return "int"; // Default
    }

    private String mapType(String type) {
        switch (type) {
            case "int": return "int";
            case "float": return "float";
            case "string": return "char*";
            case "boolean": return "int";
            default: return "void*";
        }
    }

    private String generateFunction(FunctionNode node, int indentLevel) {
        String indent = getIndent(indentLevel);
        String name = node.getName();
        String params = node.getParameters().stream()
                .map(param -> "void* " + param.getName()) // Void* placeholder for dynamic types in C
                .collect(Collectors.joining(", "));
        
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("// Future enhancement: C type inference for parameters and return type\n");
        sb.append(indent).append("void* ").append(name).append("(").append(params).append(") {\n");
        sb.append(generateNode(node.getBody(), indentLevel + 1));
        sb.append(indent).append("}\n\n");
        return sb.toString();
    }

    private String generateReturn(ReturnNode node, int indentLevel) {
        String indent = getIndent(indentLevel);
        if (node.getValue() == null) {
            return indent + "return NULL;\n";
        }
        String exprStr = generateNode(node.getValue(), 0);
        return indent + "return " + exprStr + ";\n";
    }

    private String getIndent(int level) {
        return "    ".repeat(level);
    }
}
