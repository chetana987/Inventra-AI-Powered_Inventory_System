package com.inventra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int port;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:" + port).description("Local Development"),
                        new Server().url("http://localhost").description("Docker / Nginx")))
                .info(new Info()
                        .title("Inventra API")
                        .description("""
                                AI-Powered Inventory Management System REST API
                                
                                ## Authentication
                                - Use `POST /api/auth/register` to create an account (first user becomes ADMIN)
                                - Use `POST /api/auth/login` to get a JWT token
                                - Click **Authorize** button and paste `Bearer <token>` to access secured endpoints
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Inventra Team")
                                .email("support@inventra.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://inventra.com")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste your JWT token here. Prefix not required — just the token.")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
