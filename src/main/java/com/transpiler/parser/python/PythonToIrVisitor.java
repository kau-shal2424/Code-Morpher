package com.transpiler.parser.python;

import com.transpiler.grammar.python.Python3ParserBaseVisitor;
import com.transpiler.grammar.python.Python3Parser;
import com.transpiler.ir.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visitor to convert Python ANTLR parse tree into the universal IR.
 */
public class PythonToIrVisitor extends Python3ParserBaseVisitor<Object> {
    private static final Logger logger = LoggerFactory.getLogger(PythonToIrVisitor.class);

    @Override
    public Object visit(org.antlr.v4.runtime.tree.ParseTree tree) {
        if (tree == null) {
            return null;
        }
        Object result = super.visit(tree);
        if (result instanceof IRNode && tree instanceof org.antlr.v4.runtime.ParserRuleContext) {
            org.antlr.v4.runtime.ParserRuleContext ctx = (org.antlr.v4.runtime.ParserRuleContext) tree;
            IRNode node = (IRNode) result;
            if (ctx.getStart() != null) {
                node.setStartIndex(ctx.getStart().getStartIndex());
            }
            if (ctx.getStop() != null) {
                node.setStopIndex(ctx.getStop().getStopIndex());
            }
        }
        return result;
    }

    private void setBlockBounds(BlockNode block, org.antlr.v4.runtime.ParserRuleContext defaultCtx) {
        if (block == null) return;
        if (block.getStatements().isEmpty()) {
            if (defaultCtx != null) {
                block.setStartIndex(defaultCtx.getStart().getStartIndex());
                block.setStopIndex(defaultCtx.getStop().getStopIndex());
            }
            return;
        }
        int minStart = Integer.MAX_VALUE;
        int maxStop = Integer.MIN_VALUE;
        for (StatementNode stmt : block.getStatements()) {
            minStart = Math.min(minStart, stmt.getStartIndex());
            maxStop = Math.max(maxStop, stmt.getStopIndex());
        }
        block.setStartIndex(minStart);
        block.setStopIndex(maxStop);
    }

    @Override
    public ProgramNode visitFile_input(Python3Parser.File_inputContext ctx) {
        logger.debug("Visiting file_input");
        ProgramNode program = new ProgramNode();
        BlockNode mainBlock = new BlockNode();

        if (ctx.stmt() != null) {
            for (Python3Parser.StmtContext stmtCtx : ctx.stmt()) {
                Object stmt = visit(stmtCtx);
                if (stmt instanceof FunctionNode) {
                    program.addChild((FunctionNode) stmt);
                } else if (stmt instanceof StatementNode) {
                    mainBlock.addStatement((StatementNode) stmt);
                } else if (stmt != null) {
                    logger.warn("Unexpected statement type: {}", stmt.getClass().getSimpleName());
                }
            }
        }

        program.addChild(mainBlock);
        
        // Explicitly set source ranges for manually constructed blocks/program
        if (ctx.getStart() != null) {
            program.setStartIndex(ctx.getStart().getStartIndex());
            mainBlock.setStartIndex(ctx.getStart().getStartIndex());
        }
        if (ctx.getStop() != null) {
            program.setStopIndex(ctx.getStop().getStopIndex());
            mainBlock.setStopIndex(ctx.getStop().getStopIndex());
        }
        
        return program;
    }

    @Override
    public Object visitStmt(Python3Parser.StmtContext ctx) {
        if (ctx.if_stmt() != null) {
            return visit(ctx.if_stmt());
        } else if (ctx.for_stmt() != null) {
            return visit(ctx.for_stmt());
        } else if (ctx.while_stmt() != null) {
            return visit(ctx.while_stmt());
        } else if (ctx.func_def() != null) {
            return visit(ctx.func_def());
        } else if (ctx.return_stmt() != null) {
            return visit(ctx.return_stmt());
        }
        return visit(ctx.simple_stmt());
    }

    @Override
    public FunctionNode visitFunc_def(Python3Parser.Func_defContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        logger.debug("Visiting func_def: {}", name);
        
        BlockNode body = new BlockNode();
        if (ctx.stmt() != null) {
            for (Python3Parser.StmtContext stmtCtx : ctx.stmt()) {
                Object stmt = visit(stmtCtx);
                if (stmt instanceof StatementNode) {
                    body.addStatement((StatementNode) stmt);
                }
            }
        }
        setBlockBounds(body, ctx);
        
        FunctionNode func = new FunctionNode(name, "dynamic", body);
        
        if (ctx.parameters() != null) {
            for (Python3Parser.ParameterContext paramCtx : ctx.parameters().parameter()) {
                String paramName = paramCtx.IDENTIFIER().getText();
                VariableDeclNode paramNode = new VariableDeclNode("dynamic", paramName);
                if (paramCtx.getStart() != null) {
                    paramNode.setStartIndex(paramCtx.getStart().getStartIndex());
                }
                if (paramCtx.getStop() != null) {
                    paramNode.setStopIndex(paramCtx.getStop().getStopIndex());
                }
                func.addParameter(paramNode);
            }
        }
        
        return func;
    }

    @Override
    public ReturnNode visitReturn_stmt(Python3Parser.Return_stmtContext ctx) {
        logger.debug("Visiting return_stmt");
        ExpressionNode value = null;
        if (ctx.expr() != null) {
            value = (ExpressionNode) visit(ctx.expr());
        }
        return new ReturnNode(value);
    }

    @Override
    public StatementNode visitSimple_stmt(Python3Parser.Simple_stmtContext ctx) {
        if (ctx.assignment() != null) {
            return (StatementNode) visit(ctx.assignment());
        } else if (ctx.expr_stmt() != null) {
            return (StatementNode) visit(ctx.expr_stmt());
        }
        throw new UnsupportedOperationException("Unsupported simple statement");
    }

    @Override
    public IfNode visitIf_stmt(Python3Parser.If_stmtContext ctx) {
        ExpressionNode condition = (ExpressionNode) visit(ctx.expr());
        logger.debug("Visiting if_stmt with condition: {}", condition);
        BlockNode thenBlock = new BlockNode();
        
        int elseIndex = -1;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals("else")) {
                elseIndex = i;
                break;
            }
        }
        
        BlockNode currentBlock = thenBlock;
        BlockNode elseBlock = null;
        
        for (Python3Parser.StmtContext stmtCtx : ctx.stmt()) {
            if (elseIndex != -1 && stmtCtx.getStart().getTokenIndex() > ctx.getChild(elseIndex).getSourceInterval().a) {
                if (elseBlock == null) {
                    elseBlock = new BlockNode();
                    currentBlock = elseBlock;
                }
            }
            Object result = visit(stmtCtx);
            if (result instanceof StatementNode) {
                currentBlock.addStatement((StatementNode) result);
            }
        }
        
        setBlockBounds(thenBlock, ctx);
        if (elseBlock != null) {
            setBlockBounds(elseBlock, ctx);
        }
        
        return new IfNode(condition, thenBlock, elseBlock);
    }

    @Override
    public WhileNode visitWhile_stmt(Python3Parser.While_stmtContext ctx) {
        ExpressionNode condition = (ExpressionNode) visit(ctx.expr());
        logger.debug("Visiting while_stmt with condition: {}", condition);
        BlockNode body = new BlockNode();
        for (Python3Parser.StmtContext stmtCtx : ctx.stmt()) {
            Object result = visit(stmtCtx);
            if (result instanceof StatementNode) {
                body.addStatement((StatementNode) result);
            }
        }
        setBlockBounds(body, ctx);
        return new WhileNode(condition, body);
    }

    @Override
    public ForNode visitFor_stmt(Python3Parser.For_stmtContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        
        Python3Parser.Range_exprContext rangeCtx = ctx.range_expr();
        ExpressionNode start = null;
        ExpressionNode end = null;
        
        if (rangeCtx.expr().size() == 1) {
            end = (ExpressionNode) visit(rangeCtx.expr(0));
        } else if (rangeCtx.expr().size() == 2) {
            start = (ExpressionNode) visit(rangeCtx.expr(0));
            end = (ExpressionNode) visit(rangeCtx.expr(1));
        }
        
        logger.debug("Visiting for_stmt: for {} in range({}, {})", varName, start, end);
        
        BlockNode body = new BlockNode();
        for (Python3Parser.StmtContext stmtCtx : ctx.stmt()) {
            Object result = visit(stmtCtx);
            if (result instanceof StatementNode) {
                body.addStatement((StatementNode) result);
            }
        }
        setBlockBounds(body, ctx);
        return new ForNode(varName, start, end, body);
    }

    @Override
    public Object visitRange_expr(Python3Parser.Range_exprContext ctx) {
        return null; // Handled directly in visitFor_stmt
    }

    @Override
    public AssignmentNode visitAssignment(Python3Parser.AssignmentContext ctx) {
        String target = ctx.IDENTIFIER().getText();
        ExpressionNode value = (ExpressionNode) visit(ctx.expr());
        logger.debug("Visiting assignment: {} = {}", target, value);
        return new AssignmentNode(target, value);
    }

    @Override
    public ExpressionStatementNode visitExpr_stmt(Python3Parser.Expr_stmtContext ctx) {
        ExpressionNode expr = (ExpressionNode) visit(ctx.expr());
        return new ExpressionStatementNode(expr);
    }

    @Override
    public ExpressionNode visitExpr(Python3Parser.ExprContext ctx) {
        return (ExpressionNode) visit(ctx.comparison());
    }

    @Override
    public ExpressionNode visitComparison(Python3Parser.ComparisonContext ctx) {
        // If there are no operators, just pass through
        if (ctx.addition().size() == 1) {
            return (ExpressionNode) visit(ctx.addition(0));
        }
        // Build left-associative tree from the list
        ExpressionNode left = (ExpressionNode) visit(ctx.addition(0));
        for (int i = 0; i < ctx.getChildCount() - 1; i++) {
            // Walk through operators and right-hand sides
        }
        // Simple two-operand case (most common)
        String op = ctx.getChild(1).getText();
        ExpressionNode right = (ExpressionNode) visit(ctx.addition(1));
        logger.debug("Visiting comparison: {} {} {}", left, op, right);
        return new BinaryOpNode(left, op, right);
    }

    @Override
    public ExpressionNode visitAddition(Python3Parser.AdditionContext ctx) {
        if (ctx.multiplication().size() == 1) {
            return (ExpressionNode) visit(ctx.multiplication(0));
        }
        // Build left-associative binary tree
        ExpressionNode result = (ExpressionNode) visit(ctx.multiplication(0));
        int opIndex = 1;
        for (int i = 1; i < ctx.multiplication().size(); i++) {
            String op = ctx.getChild(opIndex).getText();
            ExpressionNode right = (ExpressionNode) visit(ctx.multiplication(i));
            result = new BinaryOpNode(result, op, right);
            opIndex += 2;
        }
        logger.debug("Visiting addition: {}", result);
        return result;
    }

    @Override
    public ExpressionNode visitMultiplication(Python3Parser.MultiplicationContext ctx) {
        if (ctx.unary().size() == 1) {
            return (ExpressionNode) visit(ctx.unary(0));
        }
        // Build left-associative binary tree
        ExpressionNode result = (ExpressionNode) visit(ctx.unary(0));
        int opIndex = 1;
        for (int i = 1; i < ctx.unary().size(); i++) {
            String op = ctx.getChild(opIndex).getText();
            ExpressionNode right = (ExpressionNode) visit(ctx.unary(i));
            result = new BinaryOpNode(result, op, right);
            opIndex += 2;
        }
        logger.debug("Visiting multiplication: {}", result);
        return result;
    }

    @Override
    public ExpressionNode visitUnary(Python3Parser.UnaryContext ctx) {
        if (ctx.atom() != null) {
            return (ExpressionNode) visit(ctx.atom());
        } else if (ctx.func_call() != null) {
            return (ExpressionNode) visit(ctx.func_call());
        } else if (ctx.expr() != null) {
            // parenthesised expression
            return (ExpressionNode) visit(ctx.expr());
        }
        throw new UnsupportedOperationException("Unsupported unary expression");
    }

    @Override
    public ExpressionNode visitAtom(Python3Parser.AtomContext ctx) {
        if (ctx.IDENTIFIER() != null) {
            return new VariableRefNode(ctx.IDENTIFIER().getText());
        } else if (ctx.INTEGER() != null) {
            return new LiteralNode(Integer.parseInt(ctx.INTEGER().getText()), "int");
        } else if (ctx.STRING() != null) {
            String text = ctx.STRING().getText();
            // Remove surrounding quotes
            String value = text.substring(1, text.length() - 1);
            return new LiteralNode(value, "string");
        } else if (ctx.expr() != null) {
            return (ExpressionNode) visit(ctx.expr());
        }
        throw new UnsupportedOperationException("Unsupported atom");
    }

    @Override
    public FunctionCallNode visitFunc_call(Python3Parser.Func_callContext ctx) {
        // func_call allows both IDENTIFIER and PRINT as the function name token
        String name;
        if (ctx.PRINT() != null) {
            name = ctx.PRINT().getText();
        } else {
            name = ctx.IDENTIFIER().getText();
        }
        logger.debug("Visiting func_call: {}", name);
        FunctionCallNode call = new FunctionCallNode(name);
        if (ctx.expr() != null) {
            for (Python3Parser.ExprContext exprCtx : ctx.expr()) {
                call.addArgument((ExpressionNode) visit(exprCtx));
            }
        }
        return call;
    }
}
