package com.inventra.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryRequest {

    @Schema(description = "ID of the product to adjust stock for", example = "1")
    @NotNull(message = "Product ID is required")
    private Long productId;

    @Schema(description = "Quantity to add or remove (must be at least 1)", example = "50")
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Schema(description = "Optional remarks or reason for the transaction", example = "Restock from supplier")
    private String remarks;
}
