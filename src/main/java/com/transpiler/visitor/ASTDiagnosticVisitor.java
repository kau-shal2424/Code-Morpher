package com.transpiler.visitor;

import com.transpiler.dto.CompilerErrorDto;
import com.transpiler.ir.*;

import java.util.*;

/**
 * Perform semantic analysis walks over the immutable AST to find warnings/errors
 * (e.g., division by zero, undeclared variable use, empty blocks, infinite loops).
 */
public class ASTDiagnosticVisitor {

    public List<CompilerErrorDto> collect(IRNode root) {
        List<CompilerErrorDto> diagnostics = new ArrayList<>();
        Set<String> declaredVariables = new HashSet<>();
        
        // Built-in functions/variables in our transpiler vocabulary
        declaredVariables.add("print");
        declaredVariables.add("range");
        
        analyze(root, declaredVariables, diagnostics);
        return diagnostics;
    }

    private void analyze(IRNode node, Set<String> declaredVars, List<CompilerErrorDto> diagnostics) {
        if (node == null) return;

        if (node instanceof AssignmentNode) {
            AssignmentNode assign = (AssignmentNode) node;
            // Record declaration/assignment of target variable
            declaredVars.add(assign.getTarget());
            // Analyze the value expression
            analyze(assign.getValue(), declaredVars, diagnostics);
            
        } else if (node instanceof VariableRefNode) {
            VariableRefNode ref = (VariableRefNode) node;
            if (!declaredVars.contains(ref.getName())) {
                diagnostics.add(new CompilerErrorDto(
                    0, 0, // Frontend can locate using character indices
                    "Potential warning: Variable '" + ref.getName() + "' might be referenced before assignment",
                    "WARNING",
                    ref.getStartIndex(),
                    ref.getStopIndex()
                ));
            }
            
        } else if (node instanceof BinaryOpNode) {
            BinaryOpNode binOp = (BinaryOpNode) node;
            // Division by zero check
            if (("/".equals(binOp.getOperator()) || "%".equals(binOp.getOperator())) 
                    && binOp.getRight() instanceof LiteralNode) {
                LiteralNode lit = (LiteralNode) binOp.getRight();
                if (lit.getValue() instanceof Number && ((Number) lit.getValue()).doubleValue() == 0.0) {
                    diagnostics.add(new CompilerErrorDto(
                        0, 0,
                        "Semantic Warning: Division by zero is undefined",
                        "WARNING",
                        binOp.getRight().getStartIndex(),
                        binOp.getRight().getStopIndex()
                    ));
                }
            }
            analyze(binOp.getLeft(), declaredVars, diagnostics);
            analyze(binOp.getRight(), declaredVars, diagnostics);
            
        } else if (node instanceof WhileNode) {
            WhileNode whileNode = (WhileNode) node;
            // Infinite loop check
            if (whileNode.getCondition() instanceof LiteralNode) {
                LiteralNode lit = (LiteralNode) whileNode.getCondition();
                if (Boolean.TRUE.equals(lit.getValue()) || "True".equals(lit.getValue())) {
                    diagnostics.add(new CompilerErrorDto(
                        0, 0,
                        "Performance Warning: Infinite loop detected ('while True')",
                        "WARNING",
                        whileNode.getCondition().getStartIndex(),
                        whileNode.getCondition().getStopIndex()
                    ));
                }
            }
            analyze(whileNode.getCondition(), declaredVars, diagnostics);
            analyze(whileNode.getBody(), declaredVars, diagnostics);
            
        } else if (node instanceof BlockNode) {
            BlockNode block = (BlockNode) node;
            if (block.getStatements().isEmpty()) {
                diagnostics.add(new CompilerErrorDto(
                    0, 0,
                    "Code Quality Warning: Empty block of code detected",
                    "WARNING",
                    block.getStartIndex(),
                    block.getStopIndex()
                ));
            } else {
                for (StatementNode stmt : block.getStatements()) {
                    analyze(stmt, declaredVars, diagnostics);
                }
            }
            
        } else if (node instanceof ProgramNode) {
            ProgramNode program = (ProgramNode) node;
            for (IRNode child : program.getChildren()) {
                analyze(child, declaredVars, diagnostics);
            }
            
        } else if (node instanceof FunctionNode) {
            FunctionNode func = (FunctionNode) node;
            // Create nested scope for parameters
            Set<String> localScope = new HashSet<>(declaredVars);
            if (func.getParameters() != null) {
                for (VariableDeclNode param : func.getParameters()) {
                    localScope.add(param.getName());
                }
            }
            analyze(func.getBody(), localScope, diagnostics);
            
        } else if (node instanceof ForNode) {
            ForNode forNode = (ForNode) node;
            Set<String> localScope = new HashSet<>(declaredVars);
            localScope.add(forNode.getVariableName());
            analyze(forNode.getStartExpression(), localScope, diagnostics);
            analyze(forNode.getEndExpression(), localScope, diagnostics);
            analyze(forNode.getBody(), localScope, diagnostics);
            
        } else if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;
            analyze(ifNode.getCondition(), declaredVars, diagnostics);
            analyze(ifNode.getThenBlock(), declaredVars, diagnostics);
            analyze(ifNode.getElseBlock(), declaredVars, diagnostics);
            
        } else if (node instanceof VariableDeclNode) {
            VariableDeclNode decl = (VariableDeclNode) node;
            declaredVars.add(decl.getName());
            analyze(decl.getInitializer(), declaredVars, diagnostics);
            
        } else if (node instanceof ReturnNode) {
            ReturnNode ret = (ReturnNode) node;
            analyze(ret.getValue(), declaredVars, diagnostics);
            
        } else if (node instanceof UnaryOpNode) {
            UnaryOpNode unary = (UnaryOpNode) node;
            analyze(unary.getOperand(), declaredVars, diagnostics);
            
        } else if (node instanceof FunctionCallNode) {
            FunctionCallNode call = (FunctionCallNode) node;
            if (call.getArguments() != null) {
                for (ExpressionNode arg : call.getArguments()) {
                    analyze(arg, declaredVars, diagnostics);
                }
            }
        }
    }
}
