package com.inventra.service;

import com.inventra.dto.response.AiQueryResponse;
import com.inventra.dto.response.InventoryResponse;
import com.inventra.dto.response.LowStockProductResponse;
import com.inventra.dto.response.ProductResponse;
import com.inventra.entity.Product;
import com.inventra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final GeminiService geminiService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public AiQueryResponse processQuestion(String question) {
        Map<String, Object> classification = geminiService.classifyIntent(question);
        String intent = (String) classification.get("intent");
        Map<String, Object> params = (Map<String, Object>) classification.getOrDefault("parameters", Map.of());

        return switch (intent) {
            case "LOW_STOCK_PRODUCTS" -> handleLowStock(question);
            case "PRODUCTS_BELOW_QUANTITY" -> handleBelowQuantity(question, params);
            case "HIGHEST_STOCK_PRODUCT" -> handleHighestStock(question);
            case "RECENT_TRANSACTIONS" -> handleRecentTransactions(question);
            case "TOTAL_PRODUCTS_COUNT" -> handleTotalCount(question);
            default -> unknownIntent(question);
        };
    }

    private AiQueryResponse handleLowStock(String question) {
        List<LowStockProductResponse> products = productService.getLowStockProducts();
        String summary = products.isEmpty()
                ? "All products are sufficiently stocked."
                : "Found " + products.size() + " product(s) below minimum stock level.";
        return AiQueryResponse.builder()
                .question(question)
                .intent("LOW_STOCK_PRODUCTS")
                .summary(summary)
                .data(products)
                .build();
    }

    private AiQueryResponse handleBelowQuantity(String question, Map<String, Object> params) {
        int quantity = params.containsKey("quantity")
                ? ((Number) params.get("quantity")).intValue()
                : 10;

        List<Product> products = productRepository.findByQuantityLessThanEqual(quantity);
        String summary = products.isEmpty()
                ? "No products found with quantity " + quantity + " or below."
                : "Found " + products.size() + " product(s) with quantity " + quantity + " or below.";
        return AiQueryResponse.builder()
                .question(question)
                .intent("PRODUCTS_BELOW_QUANTITY")
                .summary(summary)
                .data(products.stream().map(ProductResponse::fromEntity).toList())
                .build();
    }

    private AiQueryResponse handleHighestStock(String question) {
        var product = productRepository.findTopByOrderByQuantityDesc();
        String summary = product
                .map(p -> "Highest stock product: " + p.getName()
                        + " (" + p.getProductCode() + ") with quantity " + p.getQuantity() + ".")
                .orElse("No products found.");
        return AiQueryResponse.builder()
                .question(question)
                .intent("HIGHEST_STOCK_PRODUCT")
                .summary(summary)
                .data(product.map(ProductResponse::fromEntity).orElse(null))
                .build();
    }

    private AiQueryResponse handleRecentTransactions(String question) {
        var page = inventoryService.getHistory(null, 0, 10);
        String summary = "Showing " + page.getContent().size() + " recent transaction(s).";
        return AiQueryResponse.builder()
                .question(question)
                .intent("RECENT_TRANSACTIONS")
                .summary(summary)
                .data(page.getContent())
                .build();
    }

    private AiQueryResponse handleTotalCount(String question) {
        long count = productRepository.count();
        String summary = "Total products available: " + count + ".";
        return AiQueryResponse.builder()
                .question(question)
                .intent("TOTAL_PRODUCTS_COUNT")
                .summary(summary)
                .data(Map.of("totalProducts", count))
                .build();
    }

    private AiQueryResponse unknownIntent(String question) {
        return AiQueryResponse.builder()
                .question(question)
                .intent("UNKNOWN")
                .summary("I couldn't understand your question. Try asking about low stock, product quantities, recent transactions, or total product count.")
                .build();
    }
}
