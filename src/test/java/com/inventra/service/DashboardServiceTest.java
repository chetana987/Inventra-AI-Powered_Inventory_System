package com.inventra.service;

import com.inventra.dto.response.DashboardResponse;
import com.inventra.dto.response.InventoryResponse;
import com.inventra.dto.response.LowStockProductResponse;
import com.inventra.entity.*;
import com.inventra.repository.InventoryTransactionRepository;
import com.inventra.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private InventoryTransactionRepository transactionRepository;
    @InjectMocks private DashboardService dashboardService;

    private Product product(Long id, String code, String name, int qty, Integer minStock) {
        return Product.builder()
                .id(id)
                .productCode(code)
                .name(name)
                .category("Electronics")
                .price(BigDecimal.TEN)
                .quantity(qty)
                .minimumStockLevel(minStock)
                .build();
    }

    private User user() {
        return User.builder().id(1L).name("Admin").email("admin@test.com").role(Role.ADMIN).build();
    }

    private InventoryTransaction transaction(Product product, TransactionType type, int qty) {
        return InventoryTransaction.builder()
                .id(1L)
                .product(product)
                .transactionType(type)
                .quantity(qty)
                .remarks("test")
                .performedBy(user())
                .build();
    }

    @Test
    void getDashboard_shouldReturnAllFields() {
        Product p1 = product(1L, "PRD-001", "Widget", 50, 10);
        Product p2 = product(2L, "PRD-002", "Bolt", 3, 5);
        InventoryTransaction tx = transaction(p1, TransactionType.STOCK_IN, 20);

        when(productRepository.count()).thenReturn(2L);
        when(productRepository.totalStockQuantity()).thenReturn(53L);
        when(productRepository.findLowStockProducts()).thenReturn(List.of(p2));
        when(transactionRepository.findTop10ByOrderByTransactionDateDesc())
                .thenReturn(List.of(tx));

        DashboardResponse response = dashboardService.getDashboard();

        assertThat(response.getTotalProducts()).isEqualTo(2L);
        assertThat(response.getTotalStockQuantity()).isEqualTo(53L);
        assertThat(response.getLowStockCount()).isEqualTo(1L);
        assertThat(response.getLowStockProducts()).hasSize(1);
        assertThat(response.getLowStockProducts().get(0).getProductCode()).isEqualTo("PRD-002");
        assertThat(response.getLowStockProducts().get(0).getQuantity()).isEqualTo(3);
        assertThat(response.getRecentTransactions()).hasSize(1);
        assertThat(response.getRecentTransactions().get(0).getProductCode()).isEqualTo("PRD-001");
    }

    @Test
    void getDashboard_shouldHandleEmptyData() {
        when(productRepository.count()).thenReturn(0L);
        when(productRepository.totalStockQuantity()).thenReturn(0L);
        when(productRepository.findLowStockProducts()).thenReturn(List.of());
        when(transactionRepository.findTop10ByOrderByTransactionDateDesc()).thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard();

        assertThat(response.getTotalProducts()).isZero();
        assertThat(response.getTotalStockQuantity()).isZero();
        assertThat(response.getLowStockCount()).isZero();
        assertThat(response.getLowStockProducts()).isEmpty();
        assertThat(response.getRecentTransactions()).isEmpty();
    }
}
