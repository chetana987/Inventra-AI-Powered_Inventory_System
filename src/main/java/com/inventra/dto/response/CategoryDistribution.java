package com.inventra.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDistribution {

    @Schema(description = "Product category name", example = "Electronics")
    private String category;

    @Schema(description = "Number of products in this category", example = "8")
    private long count;

    @Schema(description = "Total stock units in this category", example = "450")
    private long totalStock;

    @Schema(description = "Percentage of total products", example = "40.0")
    private double percentage;
}
