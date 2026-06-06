package com.inventra.dto.response;

import com.inventra.entity.InventoryTransaction;
import com.inventra.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    @Schema(description = "Transaction ID", example = "1")
    private Long id;

    @Schema(description = "Product ID", example = "1")
    private Long productId;

    @Schema(description = "Product code / SKU", example = "PROD-001")
    private String productCode;

    @Schema(description = "Product name", example = "Wireless Mouse")
    private String productName;

    @Schema(description = "Transaction type", example = "STOCK_IN")
    private TransactionType transactionType;

    @Schema(description = "Quantity of stock moved", example = "50")
    private Integer quantity;

    @Schema(description = "Transaction remarks", example = "Restock from supplier")
    private String remarks;

    @Schema(description = "Date and time of the transaction")
    private LocalDateTime transactionDate;

    @Schema(description = "Name of the user who performed the transaction", example = "John Doe")
    private String performedBy;

    public static InventoryResponse fromEntity(InventoryTransaction transaction) {
        return InventoryResponse.builder()
                .id(transaction.getId())
                .productId(transaction.getProduct().getId())
                .productCode(transaction.getProduct().getProductCode())
                .productName(transaction.getProduct().getName())
                .transactionType(transaction.getTransactionType())
                .quantity(transaction.getQuantity())
                .remarks(transaction.getRemarks())
                .transactionDate(transaction.getTransactionDate())
                .performedBy(transaction.getPerformedBy().getName())
                .build();
    }
}
