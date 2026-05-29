package com.transpiler.main;

import com.transpiler.dto.ASTNodeDto;
import com.transpiler.dto.CompilationResultDto;
import com.transpiler.dto.TokenDto;
import com.transpiler.dto.TranspileResponse;
import com.transpiler.generator.CodeGenerator;
import com.transpiler.generator.GeneratorRegistry;
import com.transpiler.ir.ProgramNode;
import com.transpiler.parser.LanguageParser;
import com.transpiler.parser.ParserRegistry;
import com.transpiler.utils.Language;
import com.transpiler.visitor.ASTSerializationVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core engine that orchestrates the transpilation process.
 */
public class TranspilerEngine {
    private static final Logger logger = LoggerFactory.getLogger(TranspilerEngine.class);

    private final ExecutorService threadPool = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors()),
        new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "compiler-viz-" + count.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        }
    );

    /**
     * Transpiles source code from one language to another.
     *
     * @param sourceCode The code to transpile.
     * @param from       The source language.
     * @param to         The target language.
     * @return The transpiled code.
     */
    public String transpile(String sourceCode, Language from, Language to) {
        if (sourceCode == null || from == null || to == null) {
            throw new IllegalArgumentException("Source code and languages cannot be null");
        }

        logger.info("Starting transpilation from {} to {}", from, to);

        // 1. Get Parser
        LanguageParser parser = ParserRegistry.getParser(from);
        
        // 2. Parse source code to IR
        logger.debug("Parsing source code to IR...");
        ProgramNode ir = parser.parse(sourceCode);

        // 3. Get Generator
        CodeGenerator generator = GeneratorRegistry.getGenerator(to);

        // 4. Generate target code
        logger.debug("Generating target code from IR...");
        String targetCode = generator.generate(ir);

        logger.info("Transpilation successful");
        return targetCode;
    }

    /**
     * Overloaded method to transpile source code with optional parallelized compilation visualization.
     *
     * @param sourceCode The code to transpile.
     * @param from       The source language.
     * @param to         The target language.
     * @param visualize  Boolean flag to enable visualization pipelines.
     * @return TranspileResponse containing the generated code and optional visualization data.
     */
    public TranspileResponse transpile(String sourceCode, Language from, Language to, Boolean visualize) {
        if (visualize == null || !visualize) {
            // Safety Guarantee: Completely bypass all parallel pipelines, serialization, and timing
            String targetCode = transpile(sourceCode, from, to);
            return new TranspileResponse(targetCode);
        }

        long overallStart = System.nanoTime();
        logger.info("Starting transpilation with parallel visualization from {} to {}", from, to);
        
        // 1. Get Parser (sequential phase)
        long parseStart = System.nanoTime();
        LanguageParser parser = ParserRegistry.getParser(from);
        
        // 2. Parse source code to IR (sequential phase)
        logger.debug("Parsing source code to IR...");
        ProgramNode ir = parser.parse(sourceCode);
        long parseDuration = System.nanoTime() - parseStart;

        // 3. Get Generator
        CodeGenerator generator = GeneratorRegistry.getGenerator(to);

        // 4. Asynchronous/Parallel execution phase (immutable reads of AST)
        logger.info("Executing parallel visualization tasks...");
        
        java.util.concurrent.ConcurrentHashMap<String, Long> durations = new java.util.concurrent.ConcurrentHashMap<>();

        CompletableFuture<String> codeGenFuture = CompletableFuture.supplyAsync(
            () -> {
                long start = System.nanoTime();
                String res = generator.generate(ir);
                durations.put("codeGen", System.nanoTime() - start);
                return res;
            }, 
            threadPool
        );
        
        CompletableFuture<ASTNodeDto> astSerializationFuture = CompletableFuture.supplyAsync(
            () -> {
                long start = System.nanoTime();
                ASTNodeDto res = new ASTSerializationVisitor().serialize(ir);
                durations.put("astSerialization", System.nanoTime() - start);
                return res;
            }, 
            threadPool
        );
        
        CompletableFuture<List<TokenDto>> tokensFuture = CompletableFuture.supplyAsync(
            () -> {
                long start = System.nanoTime();
                List<TokenDto> res = extractTokens(sourceCode, from);
                durations.put("tokenExtraction", System.nanoTime() - start);
                return res;
            }, 
            threadPool
        );

        CompletableFuture<List<com.transpiler.dto.CompilerErrorDto>> diagnosticsFuture = CompletableFuture.supplyAsync(
            () -> {
                long start = System.nanoTime();
                List<com.transpiler.dto.CompilerErrorDto> res = new com.transpiler.visitor.ASTDiagnosticVisitor().collect(ir);
                durations.put("diagnostics", System.nanoTime() - start);
                return res;
            }, 
            threadPool
        );

        CompletableFuture<List<com.transpiler.dto.SymbolTableEntry>> symbolFuture = CompletableFuture.supplyAsync(
            () -> {
                long start = System.nanoTime();
                List<com.transpiler.dto.SymbolTableEntry> res = new com.transpiler.visitor.SymbolTableVisitor().collect(ir);
                durations.put("symbolCollection", System.nanoTime() - start);
                return res;
            }, 
            threadPool
        );

        // Wait for all execution branches to complete and capture total parallel join duration
        long parallelStart = System.nanoTime();
        CompletableFuture.allOf(codeGenFuture, astSerializationFuture, tokensFuture, diagnosticsFuture, symbolFuture).join();
        long parallelDuration = System.nanoTime() - parallelStart;

        String targetCode;
        ASTNodeDto astNode;
        List<TokenDto> tokens;
        List<com.transpiler.dto.CompilerErrorDto> semanticErrors;
        List<com.transpiler.dto.SymbolTableEntry> symbols;
        try {
            targetCode = codeGenFuture.get();
            astNode = astSerializationFuture.get();
            tokens = tokensFuture.get();
            semanticErrors = diagnosticsFuture.get();
            symbols = symbolFuture.get();
        } catch (Exception e) {
            logger.error("Error retrieving parallel compilation execution results: {}", e.getMessage());
            throw new RuntimeException("Parallel compilation execution failed", e);
        }

        // Merge ANTLR syntax errors (from sequential phase) with AST semantic errors (collected in parallel)
        List<com.transpiler.dto.CompilerErrorDto> allErrors = new ArrayList<>();
        if (ir != null && ir.getErrors() != null) {
            allErrors.addAll(ir.getErrors());
        }
        if (semanticErrors != null) {
            allErrors.addAll(semanticErrors);
        }

        long totalDuration = System.nanoTime() - overallStart;

        // Compile performance execution metrics
        com.transpiler.dto.CompilationMetricsDto metrics = new com.transpiler.dto.CompilationMetricsDto();
        metrics.setSequentialParseDurationNs(parseDuration);
        metrics.setCodeGenDurationNs(durations.getOrDefault("codeGen", 0L));
        metrics.setAstSerializationDurationNs(durations.getOrDefault("astSerialization", 0L));
        metrics.setTokenExtractionDurationNs(durations.getOrDefault("tokenExtraction", 0L));
        metrics.setDiagnosticsDurationNs(durations.getOrDefault("diagnostics", 0L));
        metrics.setSymbolCollectionDurationNs(durations.getOrDefault("symbolCollection", 0L));
        metrics.setTotalParallelDurationNs(parallelDuration);
        metrics.setTotalCompilationDurationNs(totalDuration);

        CompilationResultDto vizResult = new CompilationResultDto();
        vizResult.setGeneratedCode(targetCode);
        vizResult.setAst(astNode);
        vizResult.setTokens(tokens);
        vizResult.setCst(ir != null ? ir.getCst() : null);
        vizResult.setSymbols(symbols);
        vizResult.setErrors(allErrors);
        vizResult.setMetrics(metrics);

        logger.info("Transpilation and parallel visualization successful");
        return new TranspileResponse(targetCode, vizResult);
    }

    /**
     * Extracts a list of lexical Token DTOs from the source code.
     */
    private List<TokenDto> extractTokens(String sourceCode, Language lang) {
        List<TokenDto> tokenDtos = new ArrayList<>();
        if (lang == Language.PYTHON) {
            try {
                com.transpiler.parser.python.PythonIndentLexer lexer = 
                    new com.transpiler.parser.python.PythonIndentLexer(org.antlr.v4.runtime.CharStreams.fromString(sourceCode));
                org.antlr.v4.runtime.CommonTokenStream tokenStream = new org.antlr.v4.runtime.CommonTokenStream(lexer);
                tokenStream.fill();
                
                for (org.antlr.v4.runtime.Token token : tokenStream.getTokens()) {
                    // Filter EOF and whitespace/hidden channel tokens if we want, or include them with symbolic names
                    int typeId = token.getType();
                    if (typeId == org.antlr.v4.runtime.Token.EOF) {
                        continue;
                    }
                    String typeName = lexer.getVocabulary().getSymbolicName(typeId);
                    if (typeName == null) {
                        typeName = "TOKEN_" + typeId;
                    }
                    tokenDtos.add(new TokenDto(
                        typeName,
                        token.getText(),
                        token.getLine(),
                        token.getCharPositionInLine(),
                        token.getStartIndex(),
                        token.getStopIndex()
                    ));
                }
            } catch (Exception e) {
                logger.warn("Failed to extract tokens: {}", e.getMessage());
            }
        }
        return tokenDtos;
    }
}

