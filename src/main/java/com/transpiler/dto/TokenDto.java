package com.transpiler.dto;

public class TokenDto {
    private String type;
    private String text;
    private int line;
    private int column;
    private int startIndex;
    private int stopIndex;

    public TokenDto() {}

    public TokenDto(String type, String text, int line, int column, int startIndex, int stopIndex) {
        this.type = type;
        this.text = text;
        this.line = line;
        this.column = column;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }
    public int getColumn() { return column; }
    public void setColumn(int column) { this.column = column; }
    public int getStartIndex() { return startIndex; }
    public void setStartIndex(int startIndex) { this.startIndex = startIndex; }
    public int getStopIndex() { return stopIndex; }
    public void setStopIndex(int stopIndex) { this.stopIndex = stopIndex; }
}
