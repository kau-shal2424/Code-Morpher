package com.transpiler.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a block of code, typically enclosed in braces or indentation.
 */
public class BlockNode extends StatementNode {
    private final List<StatementNode> statements;

    public BlockNode() {
        this.statements = new ArrayList<>();
    }

    public void addStatement(StatementNode statement) {
        this.statements.add(statement);
    }

    public List<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return "BlockNode(\n" +
                statements.stream()
                        .map(stmt -> "  " + stmt.toString().replace("\n", "\n  "))
                        .collect(Collectors.joining("\n")) +
                "\n)";
    }
}
