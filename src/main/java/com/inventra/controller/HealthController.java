package com.inventra.controller;

import com.inventra.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(summary = "Health check", description = "Simple health check endpoint returning service name, status, and current timestamp.")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "service", "Inventra",
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
        )));
    }
}
