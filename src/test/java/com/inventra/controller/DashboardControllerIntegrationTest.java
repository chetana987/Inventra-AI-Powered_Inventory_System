package com.inventra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventra.dto.request.InventoryRequest;
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
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;

    @Test
    @Order(1)
    void setupData() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("Admin");
        reg.setEmail("admin-dashboard@test.com");
        reg.setPassword("password123");

        String regResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        adminToken = objectMapper.readTree(regResponse)
                .get("data").get("token").asText();

        ProductRequest product = new ProductRequest();
        product.setProductCode("DASH-PROD-001");
        product.setName("Dashboard Product");
        product.setCategory("Dashboard");
        product.setPrice(new BigDecimal("25.00"));
        product.setQuantity(5);
        product.setMinimumStockLevel(10);

        String prodResponse = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long prodId = objectMapper.readTree(prodResponse)
                .get("data").get("id").asLong();

        InventoryRequest stockIn = new InventoryRequest();
        stockIn.setProductId(prodId);
        stockIn.setQuantity(15);
        stockIn.setRemarks("Initial stock");

        mockMvc.perform(post("/api/inventory/stock-in")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockIn)))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void getDashboard_shouldReturnAggregatedData() throws Exception {
        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalProducts").value(1))
                .andExpect(jsonPath("$.data.totalStockQuantity").isNumber())
                .andExpect(jsonPath("$.data.lowStockCount").isNumber())
                .andExpect(jsonPath("$.data.lowStockProducts").isArray())
                .andExpect(jsonPath("$.data.recentTransactions").isArray());
    }
}
