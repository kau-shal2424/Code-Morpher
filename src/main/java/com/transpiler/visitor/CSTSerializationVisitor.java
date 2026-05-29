package com.transpiler.visitor;

import com.transpiler.dto.ParseTreeNodeDto;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializes the Concrete Syntax Tree (CST) and implements Rule Compaction
 * (bypassing intermediate single-child rule nodes to keep the tree readable).
 */
public class CSTSerializationVisitor {

    public ParseTreeNodeDto serialize(ParseTree root, Parser parser) {
        if (root == null) return null;
        return visit(root, parser);
    }

    private ParseTreeNodeDto visit(ParseTree tree, Parser parser) {
        if (tree instanceof TerminalNode) {
            TerminalNode terminal = (TerminalNode) tree;
            String text = terminal.getText();
            // Ignore EOF and newline tokens to keep CST readable
            if (terminal.getSymbol().getType() == org.antlr.v4.runtime.Token.EOF || "\n".equals(text) || "\r\n".equals(text)) {
                return null;
            }
            return new ParseTreeNodeDto("Terminal", text, new ArrayList<>());
        }

        if (tree instanceof RuleNode) {
            RuleNode ruleNode = (RuleNode) tree;
            int ruleIndex = ruleNode.getRuleContext().getRuleIndex();
            String ruleName = parser != null ? parser.getRuleNames()[ruleIndex] : "Rule_" + ruleIndex;

            List<ParseTreeNodeDto> children = new ArrayList<>();
            for (int i = 0; i < tree.getChildCount(); i++) {
                ParseTreeNodeDto childDto = visit(tree.getChild(i), parser);
                if (childDto != null) {
                    children.add(childDto);
                }
            }

            // Rule Compaction: If this rule node has exactly 1 child, and it is a rule node,
            // we bypass the current node and return the child directly (prevents deep single-child chains).
            if (children.size() == 1 && !"Terminal".equals(children.get(0).getType())) {
                return children.get(0);
            }

            return new ParseTreeNodeDto(ruleName, ruleName, children);
        }

        return null;
    }
}
