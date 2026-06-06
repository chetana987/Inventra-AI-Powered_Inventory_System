package com.inventra.service;

import com.inventra.dto.request.InventoryRequest;
import com.inventra.dto.response.InventoryResponse;
import com.inventra.dto.response.PageResponse;
import com.inventra.entity.*;
import com.inventra.exception.BadRequestException;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.InventoryTransactionRepository;
import com.inventra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryTransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#request.productId"),
            @CacheEvict(value = {"productList", "dashboard", "lowStockProducts"}, allEntries = true)
    })
    public InventoryResponse stockIn(InventoryRequest request, User performedBy) {
        Product product = productRepository.findByIdWithLock(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        product.setQuantity(product.getQuantity() + request.getQuantity());

        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(product)
                .transactionType(TransactionType.STOCK_IN)
                .quantity(request.getQuantity())
                .remarks(request.getRemarks())
                .performedBy(performedBy)
                .build();

        return InventoryResponse.fromEntity(transactionRepository.save(transaction));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#request.productId"),
            @CacheEvict(value = {"productList", "dashboard", "lowStockProducts"}, allEntries = true)
    })
    public InventoryResponse stockOut(InventoryRequest request, User performedBy) {
        Product product = productRepository.findByIdWithLock(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (product.getQuantity() < request.getQuantity()) {
            throw new BadRequestException(
                    "Insufficient stock. Available: " + product.getQuantity()
                            + ", requested: " + request.getQuantity());
        }

        product.setQuantity(product.getQuantity() - request.getQuantity());

        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(product)
                .transactionType(TransactionType.STOCK_OUT)
                .quantity(request.getQuantity())
                .remarks(request.getRemarks())
                .performedBy(performedBy)
                .build();

        return InventoryResponse.fromEntity(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> getHistory(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));

        Page<InventoryTransaction> transactionPage =
                transactionRepository.findHistory(productId, pageable);

        Page<InventoryResponse> responsePage = transactionPage.map(InventoryResponse::fromEntity);

        return PageResponse.<InventoryResponse>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();
    }
}
