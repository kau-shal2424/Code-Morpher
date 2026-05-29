package com.transpiler.dto;

public class CompilationMetricsDto {
    private long sequentialParseDurationNs;
    private long codeGenDurationNs;
    private long astSerializationDurationNs;
    private long tokenExtractionDurationNs;
    private long diagnosticsDurationNs;
    private long symbolCollectionDurationNs;
    private long totalParallelDurationNs;
    private long totalCompilationDurationNs;

    public CompilationMetricsDto() {}

    // Getters and Setters
    public long getSequentialParseDurationNs() { return sequentialParseDurationNs; }
    public void setSequentialParseDurationNs(long sequentialParseDurationNs) { this.sequentialParseDurationNs = sequentialParseDurationNs; }

    public long getCodeGenDurationNs() { return codeGenDurationNs; }
    public void setCodeGenDurationNs(long codeGenDurationNs) { this.codeGenDurationNs = codeGenDurationNs; }

    public long getAstSerializationDurationNs() { return astSerializationDurationNs; }
    public void setAstSerializationDurationNs(long astSerializationDurationNs) { this.astSerializationDurationNs = astSerializationDurationNs; }

    public long getTokenExtractionDurationNs() { return tokenExtractionDurationNs; }
    public void setTokenExtractionDurationNs(long tokenExtractionDurationNs) { this.tokenExtractionDurationNs = tokenExtractionDurationNs; }

    public long getDiagnosticsDurationNs() { return diagnosticsDurationNs; }
    public void setDiagnosticsDurationNs(long diagnosticsDurationNs) { this.diagnosticsDurationNs = diagnosticsDurationNs; }

    public long getSymbolCollectionDurationNs() { return symbolCollectionDurationNs; }
    public void setSymbolCollectionDurationNs(long symbolCollectionDurationNs) { this.symbolCollectionDurationNs = symbolCollectionDurationNs; }

    public long getTotalParallelDurationNs() { return totalParallelDurationNs; }
    public void setTotalParallelDurationNs(long totalParallelDurationNs) { this.totalParallelDurationNs = totalParallelDurationNs; }

    public long getTotalCompilationDurationNs() { return totalCompilationDurationNs; }
    public void setTotalCompilationDurationNs(long totalCompilationDurationNs) { this.totalCompilationDurationNs = totalCompilationDurationNs; }
}
