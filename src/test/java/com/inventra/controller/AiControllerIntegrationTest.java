package com.inventra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventra.dto.request.AiQueryRequest;
import com.inventra.dto.request.RegisterRequest;
import com.inventra.dto.response.AiQueryResponse;
import com.inventra.service.AiService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiService aiService;

    private static String authToken;

    @Test
    @Order(1)
    void setupAndLogin() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("Admin");
        reg.setEmail("admin-ai@test.com");
        reg.setPassword("password123");

        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        authToken = objectMapper.readTree(response)
                .get("data").get("token").asText();
    }

    @Test
    @Order(2)
    void query_shouldReturnAiResponse() throws Exception {
        AiQueryResponse mockResponse = AiQueryResponse.builder()
                .question("Show low stock products")
                .intent("LOW_STOCK_PRODUCTS")
                .summary("Found 3 product(s) below minimum stock level.")
                .build();

        when(aiService.processQuestion(anyString())).thenReturn(mockResponse);

        AiQueryRequest request = new AiQueryRequest();
        request.setQuestion("Show low stock products");

        mockMvc.perform(post("/api/ai/query")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.question").value("Show low stock products"))
                .andExpect(jsonPath("$.data.intent").value("LOW_STOCK_PRODUCTS"))
                .andExpect(jsonPath("$.data.summary").value("Found 3 product(s) below minimum stock level."));
    }

    @Test
    @Order(3)
    void query_shouldReturn400_whenQuestionBlank() throws Exception {
        AiQueryRequest request = new AiQueryRequest();
        request.setQuestion("");

        mockMvc.perform(post("/api/ai/query")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void query_shouldReturnUnknownIntent() throws Exception {
        AiQueryResponse mockResponse = AiQueryResponse.builder()
                .question("What is the weather?")
                .intent("UNKNOWN")
                .summary("I couldn't understand your question.")
                .build();

        when(aiService.processQuestion(anyString())).thenReturn(mockResponse);

        AiQueryRequest request = new AiQueryRequest();
        request.setQuestion("What is the weather?");

        mockMvc.perform(post("/api/ai/query")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.intent").value("UNKNOWN"));
    }
}
