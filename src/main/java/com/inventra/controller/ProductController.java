package com.inventra.controller;

import com.inventra.dto.request.ProductRequest;
import com.inventra.dto.response.ApiResponse;
import com.inventra.dto.response.LowStockProductResponse;
import com.inventra.dto.response.PageResponse;
import com.inventra.dto.response.ProductResponse;
import com.inventra.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create a product", description = "Admin only. Creates a new product with code, name, category, description, price, quantity, and minimum stock level.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @Operation(summary = "Update a product", description = "Admin only. Updates all fields of an existing product by ID.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated", response));
    }

    @Operation(summary = "Delete a product", description = "Admin only. Deletes a product by ID.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
    }

    @Operation(summary = "Get product by ID", description = "Returns a single product with full details.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get low-stock products", description = "Returns paginated products where current quantity is below the minimum stock level.")
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<LowStockProductResponse>>> getLowStockProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<LowStockProductResponse> response = productService.getLowStockProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "List products (paginated)", description = "Returns a paginated, sorted, and filterable list of products. Supports search by name and category.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {

        PageResponse<ProductResponse> response = productService.getAllProducts(
                page, size, sortBy, sortDir, name, category);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
