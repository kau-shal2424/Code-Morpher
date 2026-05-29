package com.transpiler.dto;

import java.util.List;

public class ParseTreeNodeDto {
    private String type;
    private String label;
    private List<ParseTreeNodeDto> children;

    public ParseTreeNodeDto() {}

    public ParseTreeNodeDto(String type, String label, List<ParseTreeNodeDto> children) {
        this.type = type;
        this.label = label;
        this.children = children;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public List<ParseTreeNodeDto> getChildren() { return children; }
    public void setChildren(List<ParseTreeNodeDto> children) { this.children = children; }
}
