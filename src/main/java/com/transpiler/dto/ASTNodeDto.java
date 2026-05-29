package com.transpiler.dto;

import java.util.List;
import java.util.Map;

public class ASTNodeDto {
    private String type;
    private String label;
    private int startIndex;
    private int stopIndex;
    private Map<String, Object> properties;
    private List<ASTNodeDto> children;

    public ASTNodeDto() {}

    public ASTNodeDto(String type, String label, int startIndex, int stopIndex, Map<String, Object> properties, List<ASTNodeDto> children) {
        this.type = type;
        this.label = label;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.properties = properties;
        this.children = children;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public int getStartIndex() { return startIndex; }
    public void setStartIndex(int startIndex) { this.startIndex = startIndex; }
    public int getStopIndex() { return stopIndex; }
    public void setStopIndex(int stopIndex) { this.stopIndex = stopIndex; }
    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }
    public List<ASTNodeDto> getChildren() { return children; }
    public void setChildren(List<ASTNodeDto> children) { this.children = children; }
}
