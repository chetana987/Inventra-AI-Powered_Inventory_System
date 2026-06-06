package com.inventra.service;

import com.inventra.dto.response.CategoryDistribution;
import com.inventra.dto.response.ChartDataPoint;
import com.inventra.dto.response.DashboardResponse;
import com.inventra.dto.response.InventoryResponse;
import com.inventra.dto.response.LowStockProductResponse;
import com.inventra.entity.InventoryTransaction;
import com.inventra.entity.TransactionType;
import com.inventra.repository.InventoryTransactionRepository;
import com.inventra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard")
    public DashboardResponse getDashboard() {
        long totalProducts = productRepository.count();
        long totalStockQuantity = productRepository.totalStockQuantity();
        List<LowStockProductResponse> lowStockProducts = productRepository.findLowStockProducts()
                .stream()
                .map(LowStockProductResponse::fromEntity)
                .toList();
        List<InventoryResponse> recentTransactions = transactionRepository
                .findTop10ByOrderByTransactionDateDesc()
                .stream()
                .map(InventoryResponse::fromEntity)
                .toList();
        List<CategoryDistribution> inventoryDistribution = computeInventoryDistribution();
        List<ChartDataPoint> monthlyData = computeMonthlyChartData();

        return DashboardResponse.builder()
                .totalProducts(totalProducts)
                .totalStockQuantity(totalStockQuantity)
                .lowStockCount(lowStockProducts.size())
                .lowStockProducts(lowStockProducts.size() > 5
                        ? lowStockProducts.subList(0, 5)
                        : lowStockProducts)
                .recentTransactions(recentTransactions)
                .inventoryDistribution(inventoryDistribution)
                .monthlyTransactions(monthlyData)
                .stockMovementTrends(monthlyData)
                .build();
    }

    private List<CategoryDistribution> computeInventoryDistribution() {
        List<Object[]> rows = productRepository.countByCategory();
        if (rows.isEmpty()) return List.of();

        long grandTotal = rows.stream()
                .mapToLong(r -> (Long) r[1])
                .sum();

        return rows.stream()
                .map(r -> {
                    String category = (String) r[0];
                    long count = (Long) r[1];
                    return CategoryDistribution.builder()
                            .category(category)
                            .count(count)
                            .percentage(Math.round((double) count / grandTotal * 1000.0) / 10.0)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .toList();
    }

    private List<ChartDataPoint> computeMonthlyChartData() {
        LocalDateTime since = LocalDate.now().minusMonths(6).withDayOfMonth(1).atStartOfDay();
        List<InventoryTransaction> transactions = transactionRepository.findTransactionsSince(since);

        Map<YearMonth, ChartDataPoint> monthMap = new LinkedHashMap<>();

        YearMonth current = YearMonth.from(since);
        YearMonth end = YearMonth.now();
        while (!current.isAfter(end)) {
            String label = current.format(DateTimeFormatter.ofPattern("MMM"));
            monthMap.put(current, ChartDataPoint.builder()
                    .year(current.getYear())
                    .month(current.getMonthValue())
                    .monthLabel(label)
                    .inCount(0)
                    .outCount(0)
                    .inQuantity(0)
                    .outQuantity(0)
                    .build());
            current = current.plusMonths(1);
        }

        for (InventoryTransaction t : transactions) {
            YearMonth ym = YearMonth.from(t.getTransactionDate());
            ChartDataPoint point = monthMap.get(ym);
            if (point == null) continue;

            int qty = t.getQuantity() != null ? t.getQuantity() : 0;
            if (t.getTransactionType() == TransactionType.STOCK_IN) {
                point.setInCount(point.getInCount() + 1);
                point.setInQuantity(point.getInQuantity() + qty);
            } else {
                point.setOutCount(point.getOutCount() + 1);
                point.setOutQuantity(point.getOutQuantity() + qty);
            }
        }

        return List.copyOf(monthMap.values());
    }
}
