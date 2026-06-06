package com.inventra.service;

import com.inventra.dto.request.ProductRequest;
import com.inventra.dto.response.LowStockProductResponse;
import com.inventra.dto.response.PageResponse;
import com.inventra.dto.response.ProductResponse;
import com.inventra.entity.Product;
import com.inventra.exception.BadRequestException;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    @CacheEvict(value = {"productList", "dashboard", "lowStockProducts"}, allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new BadRequestException(
                    "Product code '" + request.getProductCode() + "' already exists");
        }

        Product product = Product.builder()
                .productCode(request.getProductCode())
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .minimumStockLevel(request.getMinimumStockLevel())
                .build();

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    @Caching(
            put = { @CachePut(value = "products", key = "#id") },
            evict = { @CacheEvict(value = {"productList", "dashboard", "lowStockProducts"}, allEntries = true) }
    )
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (!product.getProductCode().equals(request.getProductCode())
                && productRepository.existsByProductCode(request.getProductCode())) {
            throw new BadRequestException(
                    "Product code '" + request.getProductCode() + "' already exists");
        }

        product.setProductCode(request.getProductCode());
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setMinimumStockLevel(request.getMinimumStockLevel());

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = {"productList", "dashboard", "lowStockProducts"}, allEntries = true)
    })
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "productList", key = "#page + '-' + #size + '-' + #sortBy + '-' + #sortDir + '-' + (#name ?: '') + '-' + (#category ?: '')")
    public PageResponse<ProductResponse> getAllProducts(
            int page, int size, String sortBy, String sortDir,
            String name, String category) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.searchProducts(
                sanitize(name), sanitize(category), pageable);

        Page<ProductResponse> responsePage = productPage.map(ProductResponse::fromEntity);

        return PageResponse.<ProductResponse>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<LowStockProductResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts()
                .stream()
                .map(LowStockProductResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "lowStockProducts", key = "#page + '-' + #size")
    public List<LowStockProductResponse> getLowStockProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return productRepository.findLowStockProductsPageable(pageable)
                .map(LowStockProductResponse::fromEntity)
                .getContent();
    }

    private String sanitize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
