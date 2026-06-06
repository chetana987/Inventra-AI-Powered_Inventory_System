package com.inventra.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.endpoint}")
    private String endpoint;

    private static final String SYSTEM_PROMPT = """
            You are an intent classifier for an inventory management system.
            Given a user question, classify the intent and extract parameters.
            Respond with ONLY a valid JSON object, no other text.

            Intents:
            - LOW_STOCK_PRODUCTS: user asks about low-stock or below-minimum products
            - PRODUCTS_BELOW_QUANTITY: user asks for products with stock below a specific number
            - HIGHEST_STOCK_PRODUCT: user asks about the product with most stock
            - RECENT_TRANSACTIONS: user asks about recent stock movements or history
            - TOTAL_PRODUCTS_COUNT: user asks how many products exist or total count
            - UNKNOWN: question does not match any intent

            Examples:
            Q: "Which products are low in stock?"
            {"intent": "LOW_STOCK_PRODUCTS", "parameters": {}}

            Q: "Show products below quantity 20"
            {"intent": "PRODUCTS_BELOW_QUANTITY", "parameters": {"quantity": 20}}

            Q: "Which product has highest stock?"
            {"intent": "HIGHEST_STOCK_PRODUCT", "parameters": {}}

            Q: "Show recent transactions"
            {"intent": "RECENT_TRANSACTIONS", "parameters": {}}

            Q: "How many products are available?"
            {"intent": "TOTAL_PRODUCTS_COUNT", "parameters": {}}
            """;

    public Map<String, Object> classifyIntent(String question) {
        String prompt = SYSTEM_PROMPT + "\n\nQ: \"" + question + "\"\n";

        try {
            String url = endpoint + "/" + model + ":generateContent";

            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", new Object[]{part});
            Map<String, Object> requestBody = Map.of("contents", new Object[]{content});

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            Map<String, Object> result = parseResponse(response.getBody());
            if (!"UNKNOWN".equals(result.get("intent"))) {
                return result;
            }
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
        }

        return localClassify(question);
    }

    private Map<String, Object> localClassify(String question) {
        String q = question.toLowerCase().trim();
        Map<String, Object> params = new java.util.HashMap<>();

        if (matches(q, "low stock", "low in stock", "below minimum", "understock", "reorder", "below min", "minimum stock")) {
            return Map.of("intent", "LOW_STOCK_PRODUCTS", "parameters", params);
        }

        java.util.regex.Matcher qtyMatcher = java.util.regex.Pattern.compile("(?:below|less than|under|quantity below|stock below|with (?:less|stock) (?:than )?)(\\d+)").matcher(q);
        if (qtyMatcher.find()) {
            params.put("quantity", Integer.parseInt(qtyMatcher.group(1)));
            return Map.of("intent", "PRODUCTS_BELOW_QUANTITY", "parameters", params);
        }

        if (q.matches(".*(?:below|less than|under|>?\\d+).*")) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)").matcher(q);
            if (m.find()) {
                params.put("quantity", Integer.parseInt(m.group(1)));
                return Map.of("intent", "PRODUCTS_BELOW_QUANTITY", "parameters", params);
            }
        }

        if (matches(q, "highest stock", "most stock", "most quantity", "maximum stock", "most product", "highest quantity")) {
            return Map.of("intent", "HIGHEST_STOCK_PRODUCT", "parameters", params);
        }

        if (matches(q, "recent transaction", "latest transaction", "transaction history", "stock movement", "recent activity", "recent stock", "show transaction")) {
            return Map.of("intent", "RECENT_TRANSACTIONS", "parameters", params);
        }

        if (matches(q, "total product", "how many product", "product count", "available product", "total count", "number of product", "list all product", "show all product")) {
            return Map.of("intent", "TOTAL_PRODUCTS_COUNT", "parameters", params);
        }

        return Map.of("intent", "UNKNOWN", "parameters", params);
    }

    private boolean matches(String question, String... keywords) {
        for (String kw : keywords) {
            if (question.contains(kw)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.path("candidates")
                    .get(0).path("content").path("parts")
                    .get(0).path("text").asText();

            int start = text.indexOf('{');
            int end = text.lastIndexOf('}') + 1;
            if (start >= 0 && end > start) {
                text = text.substring(start, end);
            }

            return objectMapper.readValue(text, Map.class);
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            return Map.of("intent", "UNKNOWN", "parameters", Map.of());
        }
    }
}
