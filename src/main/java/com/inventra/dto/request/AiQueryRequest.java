package com.inventra.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiQueryRequest {

    @Schema(description = "Natural language question for the AI assistant", example = "Show me all products with low stock")
    @NotBlank(message = "Question is required")
    private String question;
}
