package com.inventra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventra.dto.request.InventoryRequest;
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
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static Long productId;

    @Test
    @Order(1)
    void setupAdminAndProduct() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("Admin");
        reg.setEmail("admin-inventory@test.com");
        reg.setPassword("password123");

        String regResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        adminToken = objectMapper.readTree(regResponse)
                .get("data").get("token").asText();

        ProductRequest prodReq = new ProductRequest();
        prodReq.setProductCode("INV-PROD-001");
        prodReq.setName("Inventory Product");
        prodReq.setCategory("Test");
        prodReq.setPrice(new BigDecimal("50.00"));
        prodReq.setQuantity(10);
        prodReq.setMinimumStockLevel(5);

        String prodResponse = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prodReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        productId = objectMapper.readTree(prodResponse)
                .get("data").get("id").asLong();
    }

    @Test
    @Order(2)
    void stockIn_shouldIncreaseQuantity() throws Exception {
        InventoryRequest request = new InventoryRequest();
        request.setProductId(productId);
        request.setQuantity(20);
        request.setRemarks("Restock");

        mockMvc.perform(post("/api/inventory/stock-in")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.transactionType").value("STOCK_IN"))
                .andExpect(jsonPath("$.data.quantity").value(20))
                .andExpect(jsonPath("$.data.remarks").value("Restock"));
    }

    @Test
    @Order(3)
    void stockOut_shouldDecreaseQuantity() throws Exception {
        InventoryRequest request = new InventoryRequest();
        request.setProductId(productId);
        request.setQuantity(5);
        request.setRemarks("Sale");

        mockMvc.perform(post("/api/inventory/stock-out")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.transactionType").value("STOCK_OUT"))
                .andExpect(jsonPath("$.data.quantity").value(5));
    }

    @Test
    @Order(4)
    void stockOut_shouldFail_whenInsufficientStock() throws Exception {
        InventoryRequest request = new InventoryRequest();
        request.setProductId(productId);
        request.setQuantity(999);
        request.setRemarks("Overdraw");

        mockMvc.perform(post("/api/inventory/stock-out")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    void getHistory_shouldReturnTransactions() throws Exception {
        mockMvc.perform(get("/api/inventory/history")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    @Test
    @Order(6)
    void getHistory_shouldFilterByProductId() throws Exception {
        mockMvc.perform(get("/api/inventory/history")
                        .param("productId", String.valueOf(productId))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
