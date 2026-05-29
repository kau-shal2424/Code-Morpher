package com.transpiler.dto;

public class TranspileResponse {
    private String output;
    private CompilationResultDto visualization;

    public TranspileResponse(String output) {
        this.output = output;
    }

    public TranspileResponse(String output, CompilationResultDto visualization) {
        this.output = output;
        this.visualization = visualization;
    }

    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }

    public CompilationResultDto getVisualization() { return visualization; }
    public void setVisualization(CompilationResultDto visualization) { this.visualization = visualization; }
}

