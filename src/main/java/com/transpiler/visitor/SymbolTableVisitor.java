package com.transpiler.visitor;

import com.transpiler.dto.SymbolTableEntry;
import com.transpiler.ir.*;

import java.util.*;

/**
 * Static analysis visitor that walks the AST/IR tree to collect symbol table entries
 * (variables, parameters, functions) along with their scope, type, and coordinates.
 */
public class SymbolTableVisitor {

    public List<SymbolTableEntry> collect(IRNode root) {
        List<SymbolTableEntry> symbols = new ArrayList<>();
        Set<String> seenGlobalSymbols = new HashSet<>();
        walk(root, "Global", symbols, seenGlobalSymbols);
        return symbols;
    }

    private void walk(IRNode node, String currentScope, List<SymbolTableEntry> symbols, Set<String> seenSymbols) {
        if (node == null) return;

        if (node instanceof FunctionNode) {
            FunctionNode func = (FunctionNode) node;
            String symbolKey = currentScope + ":" + func.getName();
            
            if (!seenSymbols.contains(symbolKey)) {
                symbols.add(new SymbolTableEntry(
                    func.getName(),
                    "FUNCTION",
                    func.getReturnType() != null ? func.getReturnType() : "dynamic",
                    currentScope,
                    1, // ANTLR doesn't give direct line easily here, so we default or estimate
                    func.getStartIndex(),
                    func.getStopIndex()
                ));
                seenSymbols.add(symbolKey);
            }

            // Inside the function, create a new scope
            String localScope = "Local(" + func.getName() + ")";
            Set<String> localSeen = new HashSet<>(seenSymbols);
            
            // Add parameters
            if (func.getParameters() != null) {
                for (VariableDeclNode param : func.getParameters()) {
                    String paramKey = localScope + ":" + param.getName();
                    if (!localSeen.contains(paramKey)) {
                        symbols.add(new SymbolTableEntry(
                            param.getName(),
                            "PARAMETER",
                            param.getType() != null ? param.getType() : "dynamic",
                            localScope,
                            1,
                            param.getStartIndex(),
                            param.getStopIndex()
                        ));
                        localSeen.add(paramKey);
                    }
                }
            }
            
            walk(func.getBody(), localScope, symbols, localSeen);

        } else if (node instanceof VariableDeclNode) {
            VariableDeclNode decl = (VariableDeclNode) node;
            String symbolKey = currentScope + ":" + decl.getName();
            
            if (!seenSymbols.contains(symbolKey)) {
                symbols.add(new SymbolTableEntry(
                    decl.getName(),
                    "VARIABLE",
                    decl.getType() != null ? decl.getType() : "dynamic",
                    currentScope,
                    1,
                    decl.getStartIndex(),
                    decl.getStopIndex()
                ));
                seenSymbols.add(symbolKey);
            }
            walk(decl.getInitializer(), currentScope, symbols, seenSymbols);

        } else if (node instanceof AssignmentNode) {
            AssignmentNode assign = (AssignmentNode) node;
            String symbolKey = currentScope + ":" + assign.getTarget();
            
            if (!seenSymbols.contains(symbolKey)) {
                // Infer simple literal types if possible
                String inferredType = "dynamic";
                if (assign.getValue() instanceof LiteralNode) {
                    inferredType = ((LiteralNode) assign.getValue()).getType();
                }
                
                symbols.add(new SymbolTableEntry(
                    assign.getTarget(),
                    "VARIABLE",
                    inferredType,
                    currentScope,
                    1,
                    assign.getStartIndex(),
                    assign.getStopIndex()
                ));
                seenSymbols.add(symbolKey);
            }
            walk(assign.getValue(), currentScope, symbols, seenSymbols);

        } else if (node instanceof ForNode) {
            ForNode forNode = (ForNode) node;
            String symbolKey = currentScope + ":" + forNode.getVariableName();
            
            if (!seenSymbols.contains(symbolKey)) {
                symbols.add(new SymbolTableEntry(
                    forNode.getVariableName(),
                    "VARIABLE",
                    "int", // Loop range variables are integers in Python
                    currentScope,
                    1,
                    forNode.getStartIndex(),
                    forNode.getStopIndex()
                ));
                seenSymbols.add(symbolKey);
            }
            walk(forNode.getStartExpression(), currentScope, symbols, seenSymbols);
            walk(forNode.getEndExpression(), currentScope, symbols, seenSymbols);
            walk(forNode.getBody(), currentScope, symbols, seenSymbols);

        } else if (node instanceof BlockNode) {
            BlockNode block = (BlockNode) node;
            if (block.getStatements() != null) {
                for (StatementNode stmt : block.getStatements()) {
                    walk(stmt, currentScope, symbols, seenSymbols);
                }
            }

        } else if (node instanceof ProgramNode) {
            ProgramNode program = (ProgramNode) node;
            if (program.getChildren() != null) {
                for (IRNode child : program.getChildren()) {
                    walk(child, currentScope, symbols, seenSymbols);
                }
            }

        } else if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;
            walk(ifNode.getCondition(), currentScope, symbols, seenSymbols);
            walk(ifNode.getThenBlock(), currentScope, symbols, seenSymbols);
            walk(ifNode.getElseBlock(), currentScope, symbols, seenSymbols);

        } else if (node instanceof WhileNode) {
            WhileNode whileNode = (WhileNode) node;
            walk(whileNode.getCondition(), currentScope, symbols, seenSymbols);
            walk(whileNode.getBody(), currentScope, symbols, seenSymbols);

        } else if (node instanceof BinaryOpNode) {
            BinaryOpNode bin = (BinaryOpNode) node;
            walk(bin.getLeft(), currentScope, symbols, seenSymbols);
            walk(bin.getRight(), currentScope, symbols, seenSymbols);

        } else if (node instanceof UnaryOpNode) {
            UnaryOpNode un = (UnaryOpNode) node;
            walk(un.getOperand(), currentScope, symbols, seenSymbols);

        } else if (node instanceof ReturnNode) {
            ReturnNode ret = (ReturnNode) node;
            walk(ret.getValue(), currentScope, symbols, seenSymbols);

        } else if (node instanceof ExpressionStatementNode) {
            ExpressionStatementNode estmt = (ExpressionStatementNode) node;
            walk(estmt.getExpression(), currentScope, symbols, seenSymbols);

        } else if (node instanceof FunctionCallNode) {
            FunctionCallNode call = (FunctionCallNode) node;
            if (call.getArguments() != null) {
                for (ExpressionNode arg : call.getArguments()) {
                    walk(arg, currentScope, symbols, seenSymbols);
                }
            }
        }
    }
}
