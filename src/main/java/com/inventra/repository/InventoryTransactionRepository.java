package com.inventra.repository;

import com.inventra.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    @Query(value = "SELECT t FROM InventoryTransaction t JOIN FETCH t.product JOIN FETCH t.performedBy WHERE " +
           "(:productId IS NULL OR t.product.id = :productId) " +
           "ORDER BY t.transactionDate DESC",
           countQuery = "SELECT COUNT(t) FROM InventoryTransaction t WHERE " +
                        "(:productId IS NULL OR t.product.id = :productId)")
    Page<InventoryTransaction> findHistory(@Param("productId") Long productId,
                                           Pageable pageable);

    @Query("SELECT t FROM InventoryTransaction t JOIN FETCH t.product JOIN FETCH t.performedBy ORDER BY t.transactionDate DESC")
    List<InventoryTransaction> findTop10ByOrderByTransactionDateDesc();

    @Query("SELECT t FROM InventoryTransaction t JOIN FETCH t.product JOIN FETCH t.performedBy WHERE t.transactionDate >= :since ORDER BY t.transactionDate ASC")
    List<InventoryTransaction> findTransactionsSince(@Param("since") LocalDateTime since);
}
