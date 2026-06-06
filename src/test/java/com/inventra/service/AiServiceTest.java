package com.inventra.service;

import com.inventra.dto.response.AiQueryResponse;
import com.inventra.dto.response.InventoryResponse;
import com.inventra.dto.response.LowStockProductResponse;
import com.inventra.dto.response.ProductResponse;
import com.inventra.entity.Product;
import com.inventra.entity.TransactionType;
import com.inventra.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock private GeminiService geminiService;
    @Mock private ProductService productService;
    @Mock private InventoryService inventoryService;
    @Mock private ProductRepository productRepository;
    @InjectMocks private AiService aiService;

    private Product sampleProduct() {
        return Product.builder()
                .id(1L).productCode("PRD-001").name("Widget")
                .category("Tools").price(BigDecimal.TEN).quantity(100)
                .minimumStockLevel(10).build();
    }

    @Test
    void processQuestion_shouldHandleLowStock() {
        when(geminiService.classifyIntent("show low stock"))
                .thenReturn(Map.of("intent", "LOW_STOCK_PRODUCTS", "parameters", Map.of()));
        when(productService.getLowStockProducts()).thenReturn(List.of(
                LowStockProductResponse.builder().productCode("PRD-001").name("Widget").quantity(5).minimumStockLevel(10).build()
        ));

        AiQueryResponse response = aiService.processQuestion("show low stock");

        assertThat(response.getIntent()).isEqualTo("LOW_STOCK_PRODUCTS");
        assertThat(response.getSummary()).contains("Found 1 product(s)");
        verify(productService).getLowStockProducts();
    }

    @Test
    void processQuestion_shouldHandleLowStock_whenAllStocked() {
        when(geminiService.classifyIntent("low stock?"))
                .thenReturn(Map.of("intent", "LOW_STOCK_PRODUCTS", "parameters", Map.of()));
        when(productService.getLowStockProducts()).thenReturn(List.of());

        AiQueryResponse response = aiService.processQuestion("low stock?");

        assertThat(response.getSummary()).isEqualTo("All products are sufficiently stocked.");
    }

    @Test
    void processQuestion_shouldHandleBelowQuantity() {
        when(geminiService.classifyIntent("products below 20"))
                .thenReturn(Map.of("intent", "PRODUCTS_BELOW_QUANTITY", "parameters", Map.of("quantity", 20)));
        when(productRepository.findByQuantityLessThanEqual(20)).thenReturn(List.of(sampleProduct()));

        AiQueryResponse response = aiService.processQuestion("products below 20");

        assertThat(response.getIntent()).isEqualTo("PRODUCTS_BELOW_QUANTITY");
        assertThat(response.getSummary()).contains("Found 1 product(s)");
    }

    @Test
    void processQuestion_shouldHandleBelowQuantity_whenNoParam() {
        when(geminiService.classifyIntent("low quantity"))
                .thenReturn(Map.of("intent", "PRODUCTS_BELOW_QUANTITY", "parameters", Map.of()));
        when(productRepository.findByQuantityLessThanEqual(10)).thenReturn(List.of());

        AiQueryResponse response = aiService.processQuestion("low quantity");

        assertThat(response.getSummary()).contains("No products found");
    }

    @Test
    void processQuestion_shouldHandleHighestStock() {
        Product product = sampleProduct();
        when(geminiService.classifyIntent("highest stock"))
                .thenReturn(Map.of("intent", "HIGHEST_STOCK_PRODUCT", "parameters", Map.of()));
        when(productRepository.findTopByOrderByQuantityDesc()).thenReturn(Optional.of(product));

        AiQueryResponse response = aiService.processQuestion("highest stock");

        assertThat(response.getIntent()).isEqualTo("HIGHEST_STOCK_PRODUCT");
        assertThat(response.getSummary()).contains("Highest stock product: Widget");
        verify(productRepository, times(1)).findTopByOrderByQuantityDesc();
    }

    @Test
    void processQuestion_shouldHandleHighestStock_whenNone() {
        when(geminiService.classifyIntent("highest stock"))
                .thenReturn(Map.of("intent", "HIGHEST_STOCK_PRODUCT", "parameters", Map.of()));
        when(productRepository.findTopByOrderByQuantityDesc()).thenReturn(Optional.empty());

        AiQueryResponse response = aiService.processQuestion("highest stock");

        assertThat(response.getSummary()).isEqualTo("No products found.");
    }

    @Test
    void processQuestion_shouldHandleRecentTransactions() {
        when(inventoryService.getHistory(any(), anyInt(), anyInt())).thenReturn(
                new com.inventra.dto.response.PageResponse<>(List.of(
                        InventoryResponse.builder().id(1L).transactionType(TransactionType.STOCK_IN).quantity(10).build()
                ), 0, 10, 1L, 1, true, true)
        );

        when(geminiService.classifyIntent("recent transactions"))
                .thenReturn(Map.of("intent", "RECENT_TRANSACTIONS", "parameters", Map.of()));

        AiQueryResponse response = aiService.processQuestion("recent transactions");

        assertThat(response.getIntent()).isEqualTo("RECENT_TRANSACTIONS");
        assertThat(response.getSummary()).contains("Showing 1 recent transaction(s).");
    }

    @Test
    void processQuestion_shouldHandleTotalCount() {
        when(geminiService.classifyIntent("total products"))
                .thenReturn(Map.of("intent", "TOTAL_PRODUCTS_COUNT", "parameters", Map.of()));
        when(productRepository.count()).thenReturn(42L);

        AiQueryResponse response = aiService.processQuestion("total products");

        assertThat(response.getIntent()).isEqualTo("TOTAL_PRODUCTS_COUNT");
        assertThat(response.getSummary()).isEqualTo("Total products available: 42.");
    }

    @Test
    void processQuestion_shouldHandleUnknown() {
        when(geminiService.classifyIntent("weather?"))
                .thenReturn(Map.of("intent", "UNKNOWN", "parameters", Map.of()));

        AiQueryResponse response = aiService.processQuestion("weather?");

        assertThat(response.getIntent()).isEqualTo("UNKNOWN");
        assertThat(response.getSummary()).contains("I couldn't understand your question.");
    }

    @Test
    void processQuestion_shouldHandleGeminiFailure() {
        when(geminiService.classifyIntent("anything"))
                .thenReturn(Map.of("intent", "UNKNOWN", "parameters", Map.of()));

        AiQueryResponse response = aiService.processQuestion("anything");

        assertThat(response.getIntent()).isEqualTo("UNKNOWN");
    }
}
