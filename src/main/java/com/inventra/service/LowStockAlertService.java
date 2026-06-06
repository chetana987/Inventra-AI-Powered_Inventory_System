package com.inventra.service;

import com.inventra.entity.Product;
import com.inventra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LowStockAlertService {

    private final ProductRepository productRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(readOnly = true)
    public void checkLowStock() {
        List<Product> lowStockProducts = productRepository.findLowStockProducts();

        if (lowStockProducts.isEmpty()) {
            log.info("[{}] Low stock check complete — no products below threshold",
                    LocalDateTime.now().toLocalDate());
            return;
        }

        log.warn("[{}] Low stock alert — {} product(s) below minimum stock level:",
                LocalDateTime.now().toLocalDate(), lowStockProducts.size());

        for (Product product : lowStockProducts) {
            log.warn("  • {} ({}): quantity={}, minimumStockLevel={}",
                    product.getName(),
                    product.getProductCode(),
                    product.getQuantity(),
                    product.getMinimumStockLevel());
        }
    }
}
