package com.inventra.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    @Schema(description = "Total number of products in the system", example = "150")
    private long totalProducts;

    @Schema(description = "Sum of all product quantities", example = "12500")
    private long totalStockQuantity;

    @Schema(description = "Number of products below minimum stock level", example = "5")
    private long lowStockCount;

    @Schema(description = "List of products that are low on stock")
    private List<LowStockProductResponse> lowStockProducts;

    @Schema(description = "Most recent inventory transactions")
    private List<InventoryResponse> recentTransactions;

    @Schema(description = "Product count and stock grouped by category")
    private List<CategoryDistribution> inventoryDistribution;

    @Schema(description = "Monthly transaction counts (last 6 months)")
    private List<ChartDataPoint> monthlyTransactions;

    @Schema(description = "Monthly stock movement quantities (last 6 months)")
    private List<ChartDataPoint> stockMovementTrends;
}
