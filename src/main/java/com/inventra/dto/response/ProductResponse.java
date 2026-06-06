package com.inventra.dto.response;

import com.inventra.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    @Schema(description = "Product ID", example = "1")
    private Long id;

    @Schema(description = "Unique product code / SKU", example = "PROD-001")
    private String productCode;

    @Schema(description = "Product name", example = "Wireless Mouse")
    private String name;

    @Schema(description = "Product category", example = "Electronics")
    private String category;

    @Schema(description = "Product description", example = "Ergonomic wireless mouse with USB receiver")
    private String description;

    @Schema(description = "Unit price in USD", example = "29.99")
    private BigDecimal price;

    @Schema(description = "Current stock quantity", example = "100")
    private Integer quantity;

    @Schema(description = "Minimum stock level before low-stock alert", example = "10")
    private Integer minimumStockLevel;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public static ProductResponse fromEntity(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .name(product.getName())
                .category(product.getCategory())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .minimumStockLevel(product.getMinimumStockLevel())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
