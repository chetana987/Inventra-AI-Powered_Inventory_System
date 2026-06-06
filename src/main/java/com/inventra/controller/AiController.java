package com.inventra.controller;

import com.inventra.dto.request.AiQueryRequest;
import com.inventra.dto.response.AiQueryResponse;
import com.inventra.dto.response.ApiResponse;
import com.inventra.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "Ask the AI assistant", description = "Sends a natural language question to the Gemini-powered AI assistant. Supports inventory queries, stock operations, product info, and reporting intents.")
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<AiQueryResponse>> query(
            @Valid @RequestBody AiQueryRequest request) {
        AiQueryResponse response = aiService.processQuestion(request.getQuestion());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
