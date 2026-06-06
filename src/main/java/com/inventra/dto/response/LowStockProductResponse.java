package com.inventra.dto.response;

import com.inventra.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockProductResponse {

    @Schema(description = "Product ID", example = "1")
    private Long id;

    @Schema(description = "Product code / SKU", example = "PROD-001")
    private String productCode;

    @Schema(description = "Product name", example = "Wireless Mouse")
    private String name;

    @Schema(description = "Product category", example = "Electronics")
    private String category;

    @Schema(description = "Current stock quantity", example = "5")
    private int quantity;

    @Schema(description = "Minimum stock level threshold", example = "10")
    private int minimumStockLevel;

    public static LowStockProductResponse fromEntity(Product product) {
        return LowStockProductResponse.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .name(product.getName())
                .category(product.getCategory())
                .quantity(product.getQuantity())
                .minimumStockLevel(product.getMinimumStockLevel())
                .build();
    }
}
