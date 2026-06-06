package com.inventra.service;

import com.inventra.dto.request.InventoryRequest;
import com.inventra.dto.response.InventoryResponse;
import com.inventra.dto.response.PageResponse;
import com.inventra.entity.*;
import com.inventra.exception.BadRequestException;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.InventoryTransactionRepository;
import com.inventra.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private InventoryTransactionRepository transactionRepository;
    @Mock private ProductRepository productRepository;
    @InjectMocks private InventoryService inventoryService;
    @Captor private ArgumentCaptor<InventoryTransaction> transactionCaptor;

    private User admin() {
        return User.builder().id(1L).name("Admin").email("admin@test.com").role(Role.ADMIN).build();
    }

    private Product product(Long id, int quantity) {
        return Product.builder()
                .id(id)
                .productCode("PRD-001")
                .name("Widget")
                .quantity(quantity)
                .minimumStockLevel(5)
                .build();
    }

    private InventoryRequest request(Long productId, int quantity) {
        InventoryRequest r = new InventoryRequest();
        r.setProductId(productId);
        r.setQuantity(quantity);
        r.setRemarks("test");
        return r;
    }

    private InventoryTransaction transaction(Product product, TransactionType type, int qty, User user) {
        return InventoryTransaction.builder()
                .id(1L)
                .product(product)
                .transactionType(type)
                .quantity(qty)
                .remarks("test")
                .performedBy(user)
                .build();
    }

    @Test
    void stockIn_shouldIncreaseQuantity() {
        Product product = product(1L, 10);
        User user = admin();
        InventoryRequest request = request(1L, 5);

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));
        when(transactionRepository.save(any(InventoryTransaction.class)))
                .thenReturn(transaction(product, TransactionType.STOCK_IN, 5, user));

        InventoryResponse response = inventoryService.stockIn(request, user);

        assertThat(response.getTransactionType()).isEqualTo(TransactionType.STOCK_IN);
        assertThat(response.getQuantity()).isEqualTo(5);
        assertThat(product.getQuantity()).isEqualTo(15);
        verify(transactionRepository).save(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().getTransactionType()).isEqualTo(TransactionType.STOCK_IN);
    }

    @Test
    void stockIn_shouldThrow_whenProductNotFound() {
        when(productRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.stockIn(request(99L, 5), admin()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: '99'");
    }

    @Test
    void stockOut_shouldDecreaseQuantity() {
        Product product = product(1L, 10);
        User user = admin();
        InventoryRequest request = request(1L, 3);

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));
        when(transactionRepository.save(any(InventoryTransaction.class)))
                .thenReturn(transaction(product, TransactionType.STOCK_OUT, 3, user));

        InventoryResponse response = inventoryService.stockOut(request, user);

        assertThat(response.getTransactionType()).isEqualTo(TransactionType.STOCK_OUT);
        assertThat(product.getQuantity()).isEqualTo(7);
    }

    @Test
    void stockOut_shouldThrow_whenInsufficientStock() {
        Product product = product(1L, 2);
        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> inventoryService.stockOut(request(1L, 5), admin()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Insufficient stock. Available: 2, requested: 5");
    }

    @Test
    void stockOut_shouldThrow_whenProductNotFound() {
        when(productRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.stockOut(request(99L, 5), admin()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: '99'");
    }

    @Test
    void getHistory_shouldReturnPagedResults() {
        Product product = product(1L, 10);
        User user = admin();
        InventoryTransaction tx = transaction(product, TransactionType.STOCK_IN, 5, user);

        Page<InventoryTransaction> txPage = mock(Page.class);
        when(txPage.map(any())).thenAnswer(invocation -> {
            InventoryResponse resp = InventoryResponse.fromEntity(tx);
            Page<InventoryResponse> mapped = mock(Page.class);
            when(mapped.getContent()).thenReturn(List.of(resp));
            when(mapped.getNumber()).thenReturn(0);
            when(mapped.getSize()).thenReturn(10);
            when(mapped.getTotalElements()).thenReturn(1L);
            when(mapped.getTotalPages()).thenReturn(1);
            when(mapped.isFirst()).thenReturn(true);
            when(mapped.isLast()).thenReturn(true);
            return mapped;
        });

        when(transactionRepository.findHistory(any(), any(Pageable.class))).thenReturn(txPage);

        PageResponse<InventoryResponse> result = inventoryService.getHistory(null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTransactionType()).isEqualTo(TransactionType.STOCK_IN);
        assertThat(result.getContent().get(0).getPerformedBy()).isEqualTo("Admin");
    }
}
