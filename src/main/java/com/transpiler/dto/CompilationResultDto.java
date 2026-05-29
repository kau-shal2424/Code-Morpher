package com.transpiler.dto;

import java.util.List;

public class CompilationResultDto {
    private String generatedCode;            // Stage 5: Target code output
    private List<TokenDto> tokens;            // Stage 2: Detailed lexical token stream
    private ASTNodeDto ast;                   // Stage 4: Abstract Syntax Tree (Primary)
    private ParseTreeNodeDto cst;                       // Stage 3: Concrete Syntax Tree (Advanced)
    private List<SymbolTableEntry> symbols;                   // Stage 4: Semantic static analysis symbol map
    private List<CompilerErrorDto> errors;                    // Detailed compilation phase diagnostics
    private CompilationMetricsDto metrics;                   // Phase execution performance metrics

    public CompilationResultDto() {}

    // Getters and Setters
    public String getGeneratedCode() { return generatedCode; }
    public void setGeneratedCode(String generatedCode) { this.generatedCode = generatedCode; }
    public List<TokenDto> getTokens() { return tokens; }
    public void setTokens(List<TokenDto> tokens) { this.tokens = tokens; }
    public ASTNodeDto getAst() { return ast; }
    public void setAst(ASTNodeDto ast) { this.ast = ast; }
    public ParseTreeNodeDto getCst() { return cst; }
    public void setCst(ParseTreeNodeDto cst) { this.cst = cst; }
    public List<SymbolTableEntry> getSymbols() { return symbols; }
    public void setSymbols(List<SymbolTableEntry> symbols) { this.symbols = symbols; }
    public List<CompilerErrorDto> getErrors() { return errors; }
    public void setErrors(List<CompilerErrorDto> errors) { this.errors = errors; }
    public CompilationMetricsDto getMetrics() { return metrics; }
    public void setMetrics(CompilationMetricsDto metrics) { this.metrics = metrics; }
}
