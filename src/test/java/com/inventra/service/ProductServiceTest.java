package com.inventra.service;

import com.inventra.dto.request.ProductRequest;
import com.inventra.dto.response.LowStockProductResponse;
import com.inventra.dto.response.PageResponse;
import com.inventra.dto.response.ProductResponse;
import com.inventra.entity.Product;
import com.inventra.exception.BadRequestException;
import com.inventra.exception.ResourceNotFoundException;
import com.inventra.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @InjectMocks private ProductService productService;
    @Captor private ArgumentCaptor<Product> productCaptor;

    private ProductRequest validRequest() {
        ProductRequest r = new ProductRequest();
        r.setProductCode("PRD-001");
        r.setName("Test Product");
        r.setCategory("Electronics");
        r.setDescription("A test product");
        r.setPrice(BigDecimal.valueOf(99.99));
        r.setQuantity(50);
        r.setMinimumStockLevel(10);
        return r;
    }

    private Product savedProduct(Long id) {
        return Product.builder()
                .id(id)
                .productCode("PRD-001")
                .name("Test Product")
                .category("Electronics")
                .description("A test product")
                .price(BigDecimal.valueOf(99.99))
                .quantity(50)
                .minimumStockLevel(10)
                .build();
    }

    @Test
    void createProduct_shouldSucceed() {
        ProductRequest request = validRequest();
        when(productRepository.existsByProductCode("PRD-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct(1L));

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProductCode()).isEqualTo("PRD-001");
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getCategory()).isEqualTo("Electronics");
        assertThat(response.getDescription()).isEqualTo("A test product");
        assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
        assertThat(response.getQuantity()).isEqualTo(50);
        assertThat(response.getMinimumStockLevel()).isEqualTo(10);

        verify(productRepository).existsByProductCode("PRD-001");
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getProductCode()).isEqualTo("PRD-001");
    }

    @Test
    void createProduct_shouldThrow_whenDuplicateCode() {
        ProductRequest request = validRequest();
        when(productRepository.existsByProductCode("PRD-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Product code 'PRD-001' already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_shouldSucceed() {
        Product existing = savedProduct(1L);
        ProductRequest request = validRequest();
        request.setName("Updated Name");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);

        ProductResponse response = productService.updateProduct(1L, request);

        assertThat(response.getName()).isEqualTo("Updated Name");
        verify(productRepository).save(existing);
    }

    @Test
    void updateProduct_shouldThrow_whenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, validRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: '99'");
    }

    @Test
    void updateProduct_shouldThrow_whenDuplicateCodeOnChange() {
        Product existing = savedProduct(1L);
        existing.setProductCode("PRD-OLD");

        ProductRequest request = validRequest();
        request.setProductCode("PRD-001");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsByProductCode("PRD-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.updateProduct(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Product code 'PRD-001' already exists");
    }

    @Test
    void updateProduct_shouldSkipCodeCheck_whenCodeUnchanged() {
        Product existing = savedProduct(1L);
        ProductRequest request = validRequest();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);

        ProductResponse response = productService.updateProduct(1L, request);

        assertThat(response.getProductCode()).isEqualTo("PRD-001");
        verify(productRepository, never()).existsByProductCode(any());
    }

    @Test
    void deleteProduct_shouldSucceed() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_shouldThrow_whenNotFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: '99'");

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void getProductById_shouldSucceed() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(savedProduct(1L)));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProductCode()).isEqualTo("PRD-001");
    }

    @Test
    void getProductById_shouldThrow_whenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: '99'");
    }

    @Test
    void getAllProducts_shouldReturnPagedResults() {
        Product product = savedProduct(1L);
        Page<Product> productPage = mock(Page.class);
        when(productPage.map(any())).thenAnswer(invocation -> {
            ProductResponse response = ProductResponse.fromEntity(product);
            Page<ProductResponse> mappedPage = mock(Page.class);
            when(mappedPage.getContent()).thenReturn(List.of(response));
            when(mappedPage.getNumber()).thenReturn(0);
            when(mappedPage.getSize()).thenReturn(10);
            when(mappedPage.getTotalElements()).thenReturn(1L);
            when(mappedPage.getTotalPages()).thenReturn(1);
            when(mappedPage.isFirst()).thenReturn(true);
            when(mappedPage.isLast()).thenReturn(true);
            return mappedPage;
        });

        when(productRepository.searchProducts(any(), any(), any(Pageable.class)))
                .thenReturn(productPage);

        PageResponse<ProductResponse> result = productService.getAllProducts(0, 10, "id", "asc", null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProductCode()).isEqualTo("PRD-001");
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getPage()).isZero();
        verify(productRepository).searchProducts(eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void getAllProducts_shouldApplySorting() {
        Page<Product> emptyPage = mock(Page.class);
        when(emptyPage.map(any())).thenAnswer(invocation -> {
            Page<ProductResponse> mapped = mock(Page.class);
            when(mapped.getContent()).thenReturn(List.of());
            when(mapped.getNumber()).thenReturn(0);
            when(mapped.getSize()).thenReturn(10);
            when(mapped.getTotalElements()).thenReturn(0L);
            when(mapped.getTotalPages()).thenReturn(0);
            when(mapped.isFirst()).thenReturn(true);
            when(mapped.isLast()).thenReturn(true);
            return mapped;
        });

        when(productRepository.searchProducts(any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        productService.getAllProducts(0, 10, "name", "desc", null, null);

        verify(productRepository).searchProducts(any(), any(),
                argThat(p -> p.getSort().equals(Sort.by("name").descending())));
    }

    @Test
    void getLowStockProducts_shouldReturnList() {
        Product lowStock = savedProduct(1L);
        lowStock.setQuantity(3);
        when(productRepository.findLowStockProducts()).thenReturn(List.of(lowStock));

        List<LowStockProductResponse> result = productService.getLowStockProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void getLowStockProducts_shouldReturnEmpty_whenNone() {
        when(productRepository.findLowStockProducts()).thenReturn(List.of());

        List<LowStockProductResponse> result = productService.getLowStockProducts();

        assertThat(result).isEmpty();
    }
}
