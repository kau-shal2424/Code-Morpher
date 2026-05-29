package com.transpiler.controller;

import com.transpiler.dto.TranspileRequest;
import com.transpiler.dto.TranspileResponse;
import com.transpiler.main.TranspilerEngine;
import com.transpiler.utils.Language;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}) // Allow React Frontend
public class TranspilerController {

    private final TranspilerEngine engine = new TranspilerEngine();

    @PostMapping("/transpile")
    public TranspileResponse transpile(@RequestBody TranspileRequest request) {
        Language source = Language.valueOf(request.getSourceLanguage().toUpperCase());
        Language target = Language.valueOf(request.getTargetLanguage().toUpperCase());
        
        TranspileResponse response = engine.transpile(request.getCode(), source, target, request.getVisualize());
        return response;
    }
}
