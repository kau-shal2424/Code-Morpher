package com.transpiler.dto;

public class CompilerErrorDto {
    private int line;
    private int column;
    private String message;
    private String severity; // "ERROR" or "WARNING"
    private int startIndex;
    private int stopIndex;

    public CompilerErrorDto() {}

    public CompilerErrorDto(int line, int column, String message, String severity, int startIndex, int stopIndex) {
        this.line = line;
        this.column = column;
        this.message = message;
        this.severity = severity;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }

    // Getters and Setters
    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }

    public int getColumn() { return column; }
    public void setColumn(int column) { this.column = column; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public int getStartIndex() { return startIndex; }
    public void setStartIndex(int startIndex) { this.startIndex = startIndex; }

    public int getStopIndex() { return stopIndex; }
    public void setStopIndex(int stopIndex) { this.stopIndex = stopIndex; }
}
