package com.transpiler.dto;

public class SymbolTableEntry {
    private String name;
    private String kind; // "VARIABLE", "FUNCTION", "PARAMETER"
    private String type;
    private String scope; // "Global" or "Function: name"
    private int line;
    private int startIndex;
    private int stopIndex;

    public SymbolTableEntry() {}

    public SymbolTableEntry(String name, String kind, String type, String scope, int line, int startIndex, int stopIndex) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.scope = scope;
        this.line = line;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }

    public int getStartIndex() { return startIndex; }
    public void setStartIndex(int startIndex) { this.startIndex = startIndex; }

    public int getStopIndex() { return stopIndex; }
    public void setStopIndex(int stopIndex) { this.stopIndex = stopIndex; }
}
