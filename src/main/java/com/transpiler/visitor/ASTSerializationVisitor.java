package com.transpiler.visitor;

import com.transpiler.dto.ASTNodeDto;
import com.transpiler.ir.*;

import java.util.*;

/**
 * Thread-safe visitor/serializer that performs an independent read-only traversal
 * of the AST/IR node tree to serialize it into ASTNodeDto structures.
 */
public class ASTSerializationVisitor {

    public ASTNodeDto serialize(IRNode root) {
        if (root == null) {
            return null;
        }
        return visit(root);
    }

    private ASTNodeDto visit(IRNode node) {
        if (node instanceof ProgramNode) {
            return visitProgram((ProgramNode) node);
        } else if (node instanceof FunctionNode) {
            return visitFunction((FunctionNode) node);
        } else if (node instanceof BlockNode) {
            return visitBlock((BlockNode) node);
        } else if (node instanceof VariableDeclNode) {
            return visitVariableDecl((VariableDeclNode) node);
        } else if (node instanceof AssignmentNode) {
            return visitAssignment((AssignmentNode) node);
        } else if (node instanceof ExpressionStatementNode) {
            return visitExpressionStatement((ExpressionStatementNode) node);
        } else if (node instanceof FunctionCallNode) {
            return visitFunctionCall((FunctionCallNode) node);
        } else if (node instanceof BinaryOpNode) {
            return visitBinaryOp((BinaryOpNode) node);
        } else if (node instanceof IfNode) {
            return visitIf((IfNode) node);
        } else if (node instanceof WhileNode) {
            return visitWhile((WhileNode) node);
        } else if (node instanceof ForNode) {
            return visitFor((ForNode) node);
        } else if (node instanceof VariableRefNode) {
            return visitVariableRef((VariableRefNode) node);
        } else if (node instanceof LiteralNode) {
            return visitLiteral((LiteralNode) node);
        } else if (node instanceof ReturnNode) {
            return visitReturn((ReturnNode) node);
        } else if (node instanceof UnaryOpNode) {
            return visitUnaryOp((UnaryOpNode) node);
        }

        // Fallback for custom or unknown nodes
        ASTNodeDto dto = new ASTNodeDto();
        dto.setType(node.getClass().getSimpleName());
        dto.setLabel(node.toString());
        dto.setStartIndex(node.getStartIndex());
        dto.setStopIndex(node.getStopIndex());
        dto.setChildren(new ArrayList<>());
        dto.setProperties(new HashMap<>());
        return dto;
    }

    private ASTNodeDto visitProgram(ProgramNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        if (node.getChildren() != null) {
            for (IRNode child : node.getChildren()) {
                ASTNodeDto childDto = visit(child);
                if (childDto != null) {
                    children.add(childDto);
                }
            }
        }

        Map<String, Object> props = new HashMap<>();
        return new ASTNodeDto("ProgramNode", "Program", node.getStartIndex(), node.getStopIndex(), props, children);
    }

    private ASTNodeDto visitFunction(FunctionNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        
        // Add parameters as child nodes
        if (node.getParameters() != null) {
            for (VariableDeclNode param : node.getParameters()) {
                ASTNodeDto paramDto = visit(param);
                if (paramDto != null) {
                    paramDto.setType("ParameterNode");
                    paramDto.setLabel("Param: " + param.getName());
                    children.add(paramDto);
                }
            }
        }
        
        if (node.getBody() != null) {
            ASTNodeDto bodyDto = visit(node.getBody());
            if (bodyDto != null) {
                children.add(bodyDto);
            }
        }

        Map<String, Object> props = new HashMap<>();
        props.put("name", node.getName());
        String paramsStr = node.getParameters().stream()
                .map(VariableDeclNode::getName)
                .collect(java.util.stream.Collectors.joining(", "));
        props.put("parameters", paramsStr);
        props.put("parameterCount", node.getParameters().size());
        props.put("returnType", node.getReturnType());

        return new ASTNodeDto("FunctionNode", "Function: " + node.getName() + "(" + paramsStr + ")", node.getStartIndex(), node.getStopIndex(), props, children);
    }

    private ASTNodeDto visitBlock(BlockNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        if (node.getStatements() != null) {
            for (StatementNode stmt : node.getStatements()) {
                ASTNodeDto stmtDto = visit(stmt);
                if (stmtDto != null) {
                    children.add(stmtDto);
                }
            }
        }

        Map<String, Object> props = new HashMap<>();
        return new ASTNodeDto("BlockNode", "Block", node.getStartIndex(), node.getStopIndex(), props, children);
    }

    private ASTNodeDto visitVariableDecl(VariableDeclNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        if (node.getInitializer() != null) {
            ASTNodeDto initDto = visit(node.getInitializer());
            if (initDto != null) {
                children.add(initDto);
            }
        }

        Map<String, Object> props = new HashMap<>();
        props.put("name", node.getName());
        props.put("type", node.getType());

        return new ASTNodeDto("VariableDeclNode", "Declare " + node.getName() + " (" + node.getType() + ")", node.getStartIndex(), node.getStopIndex(), props, children);
    }

    private ASTNodeDto visitAssignment(AssignmentNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        if (node.getValue() != null) {
            ASTNodeDto valueDto = visit(node.getValue());
            if (valueDto != null) {
                children.add(valueDto);
            }
        }

        Map<String, Object> props = new HashMap<>();
        props.put("target", node.getTarget());

        return new ASTNodeDto("AssignmentNode", "Assign: " + node.getTarget(), node.getStartIndex(), node.getStopIndex(), props, children);
    }

    private ASTNodeDto visitExpressionStatement(ExpressionStatementNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        if (node.getExpression() != null) {
            ASTNodeDto exprDto = visit(node.getExpression());
            if (exprDto != null) {
                children.add(exprDto);
            }
        }

        Map<String, Object> props = new HashMap<>();
        return new ASTNodeDto("ExpressionStatementNode", "ExprStatement", node.getStartIndex(), node.getStopIndex(), props, children);
    }

    private ASTNodeDto visitFunctionCall(FunctionCallNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        if (node.getArguments() != null) {
            for (ExpressionNode arg : node.getArguments()) {
                ASTNodeDto argDto = visit(arg);
                if (argDto != null) {
                    children.add(argDto);
                }
            }
        }

        Map<String, Object> props = new HashMap<>();
        props.put("functionName", node.getFunctionName());

        return new ASTNodeDto("FunctionCallNode", "Call: " + node.getFunctionName() + "()", node.getStartIndex(), node.getStopIndex(), props, children);
    }

    private ASTNodeDto visitBinaryOp(BinaryOpNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        if (node.getLeft() != null) {
            ASTNodeDto leftDto = visit(node.getLeft());
            if (leftDto != null) {
                children.add(leftDto);
            }
        }
        if (node.getRight() != null) {
            ASTNodeDto rightDto = visit(node.getRight());
            if (rightDto != null) {
                children.add(rightDto);
            }
        }

        Map<String, Object> props = new HashMap<>();
        props.put("operator", node.getOperator());

        return new ASTNodeDto("BinaryOpNode", "BinaryOp: " + node.getOperator(), node.getStartIndex(), node.getStopIndex(), props, children);
    }

    private ASTNodeDto visitIf(IfNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        if (node.getCondition() != null) {
            ASTNodeDto condDto = visit(node.getCondition());
            if (condDto != null) {
                children.add(condDto);
            }
        }
        if (node.getThenBlock() != null) {
            ASTNodeDto thenDto = visit(node.getThenBlock());
            if (thenDto != null) {
                children.add(thenDto);
            }
        }
        if (node.getElseBlock() != null) {
            ASTNodeDto elseDto = visit(node.getElseBlock());
            if (elseDto != null) {
                children.add(elseDto);
            }
        }

        Map<String, Object> props = new HashMap<>();
        return new ASTNodeDto("IfNode", "If", node.getStartIndex(), node.getStopIndex(), props, children);
    }

    private ASTNodeDto visitWhile(WhileNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        if (node.getCondition() != null) {
            ASTNodeDto condDto = visit(node.getCondition());
            if (condDto != null) {
                children.add(condDto);
            }
        }
        if (node.getBody() != null) {
            ASTNodeDto bodyDto = visit(node.getBody());
            if (bodyDto != null) {
                children.add(bodyDto);
            }
        }

        Map<String, Object> props = new HashMap<>();
        return new ASTNodeDto("WhileNode", "While", node.getStartIndex(), node.getStopIndex(), props, children);
    }

    private ASTNodeDto visitFor(ForNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        if (node.getStartExpression() != null) {
            ASTNodeDto startDto = visit(node.getStartExpression());
            if (startDto != null) {
                children.add(startDto);
            }
        }
        if (node.getEndExpression() != null) {
            ASTNodeDto endDto = visit(node.getEndExpression());
            if (endDto != null) {
                children.add(endDto);
            }
        }
        if (node.getBody() != null) {
            ASTNodeDto bodyDto = visit(node.getBody());
            if (bodyDto != null) {
                children.add(bodyDto);
            }
        }

        Map<String, Object> props = new HashMap<>();
        props.put("variableName", node.getVariableName());

        return new ASTNodeDto("ForNode", "For: " + node.getVariableName(), node.getStartIndex(), node.getStopIndex(), props, children);
    }

    private ASTNodeDto visitVariableRef(VariableRefNode node) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", node.getName());

        return new ASTNodeDto("VariableRefNode", "Ref: " + node.getName(), node.getStartIndex(), node.getStopIndex(), props, new ArrayList<>());
    }

    private ASTNodeDto visitLiteral(LiteralNode node) {
        Map<String, Object> props = new HashMap<>();
        props.put("value", node.getValue());
        props.put("type", node.getType());

        String valStr = node.getValue() != null ? String.valueOf(node.getValue()) : "null";
        return new ASTNodeDto("LiteralNode", "Literal: " + valStr, node.getStartIndex(), node.getStopIndex(), props, new ArrayList<>());
    }

    private ASTNodeDto visitReturn(ReturnNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        if (node.getValue() != null) {
            ASTNodeDto exprDto = visit(node.getValue());
            if (exprDto != null) {
                children.add(exprDto);
            }
        }

        Map<String, Object> props = new HashMap<>();
        return new ASTNodeDto("ReturnNode", "Return", node.getStartIndex(), node.getStopIndex(), props, children);
    }

    private ASTNodeDto visitUnaryOp(UnaryOpNode node) {
        List<ASTNodeDto> children = new ArrayList<>();
        if (node.getOperand() != null) {
            ASTNodeDto opDto = visit(node.getOperand());
            if (opDto != null) {
                children.add(opDto);
            }
        }

        Map<String, Object> props = new HashMap<>();
        props.put("operator", node.getOperator());

        return new ASTNodeDto("UnaryOpNode", "UnaryOp: " + node.getOperator(), node.getStartIndex(), node.getStopIndex(), props, children);
    }
}
