package com.inventra.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @Schema(description = "Unique product code / SKU", example = "PROD-001")
    @NotBlank(message = "Product code is required")
    @Size(max = 50, message = "Product code must not exceed 50 characters")
    private String productCode;

    @Schema(description = "Product display name", example = "Wireless Mouse")
    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must not exceed 150 characters")
    private String name;

    @Schema(description = "Product category", example = "Electronics")
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Schema(description = "Detailed product description", example = "Ergonomic wireless mouse with USB receiver")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Schema(description = "Unit price in USD", example = "29.99")
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer and 2 decimal digits")
    private BigDecimal price;

    @Schema(description = "Current stock quantity", example = "100")
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Schema(description = "Minimum stock level that triggers a low-stock alert", example = "10")
    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minimumStockLevel;
}
