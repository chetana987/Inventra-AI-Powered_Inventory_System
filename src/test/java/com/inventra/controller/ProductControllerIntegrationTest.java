package com.inventra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventra.dto.request.LoginRequest;
import com.inventra.dto.request.ProductRequest;
import com.inventra.dto.request.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import org.springframework.test.annotation.DirtiesContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static Long createdProductId;

    @Test
    @Order(1)
    void setupAdminAndLogin() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("Admin");
        reg.setEmail("admin-products@test.com");
        reg.setPassword("password123");

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        adminToken = objectMapper.readTree(response)
                .get("data").get("token").asText();
    }

    @Test
    @Order(2)
    void createProduct_shouldReturnCreated() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setProductCode("PROD001");
        request.setName("Test Product");
        request.setCategory("Electronics");
        request.setDescription("A test product");
        request.setPrice(new BigDecimal("99.99"));
        request.setQuantity(100);
        request.setMinimumStockLevel(10);

        String response = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.productCode").value("PROD001"))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andReturn().getResponse().getContentAsString();

        createdProductId = objectMapper.readTree(response)
                .get("data").get("id").asLong();
    }

    @Test
    @Order(3)
    void createProduct_shouldFail_whenDuplicateCode() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setProductCode("PROD001");
        request.setName("Duplicate Product");
        request.setCategory("Electronics");
        request.setPrice(new BigDecimal("49.99"));
        request.setQuantity(50);
        request.setMinimumStockLevel(5);

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Product code 'PROD001' already exists"));
    }

    @Test
    @Order(4)
    void getProductById_shouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}", createdProductId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(createdProductId))
                .andExpect(jsonPath("$.data.productCode").value("PROD001"))
                .andExpect(jsonPath("$.data.name").value("Test Product"));
    }

    @Test
    @Order(5)
    void getProductById_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 99999)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void getAllProducts_shouldReturnPagedResults() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @Order(7)
    void updateProduct_shouldSucceed() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setProductCode("PROD001");
        request.setName("Updated Product");
        request.setCategory("Electronics");
        request.setDescription("Updated description");
        request.setPrice(new BigDecimal("149.99"));
        request.setQuantity(200);
        request.setMinimumStockLevel(20);

        mockMvc.perform(put("/api/products/{id}", createdProductId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Product"));
    }

    @Test
    @Order(8)
    void deleteProduct_shouldSucceed() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", createdProductId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product deleted"));
    }

    @Test
    @Order(9)
    void createProduct_shouldFail_whenUnauthenticated() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setProductCode("PROD002");
        request.setName("Unauthorized Product");
        request.setCategory("Books");
        request.setPrice(new BigDecimal("29.99"));
        request.setQuantity(10);
        request.setMinimumStockLevel(2);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(10)
    void getLowStockProducts_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/products/low-stock")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
