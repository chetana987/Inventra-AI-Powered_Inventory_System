package com.inventra.repository;

import com.inventra.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    @Query(value = "SELECT * FROM products p WHERE " +
           "(:name IS NULL OR LOWER(p.name::text) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR LOWER(p.category::text) = LOWER(:category))",
           countQuery = "SELECT COUNT(*) FROM products p WHERE " +
           "(:name IS NULL OR LOWER(p.name::text) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:category IS NULL OR LOWER(p.category::text) = LOWER(:category))",
           nativeQuery = true)
    Page<Product> searchProducts(@Param("name") String name,
                                 @Param("category") String category,
                                 Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM Product p")
    long totalStockQuantity();

    @Query("SELECT p FROM Product p WHERE p.minimumStockLevel IS NOT NULL AND p.quantity <= p.minimumStockLevel")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.minimumStockLevel IS NOT NULL AND p.quantity <= p.minimumStockLevel")
    Page<Product> findLowStockProductsPageable(Pageable pageable);

    List<Product> findByQuantityLessThanEqual(int quantity);

    @Query("SELECT p FROM Product p ORDER BY p.quantity DESC LIMIT 1")
    Optional<Product> findTopByOrderByQuantityDesc();

    @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category")
    List<Object[]> countByCategory();
}
