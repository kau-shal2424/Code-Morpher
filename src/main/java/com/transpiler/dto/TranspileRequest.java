package com.transpiler.dto;

public class TranspileRequest {
    private String sourceLanguage;
    private String targetLanguage;
    private String code;
    private Boolean visualize;

    // Getters and Setters
    public String getSourceLanguage() { return sourceLanguage; }
    public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }
    
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Boolean getVisualize() { return visualize; }
    public void setVisualize(Boolean visualize) { this.visualize = visualize; }
}

