package com.inventra.dto.response;

import com.inventra.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    @Schema(description = "JWT bearer token for authentication", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "User's full name", example = "John Doe")
    private String name;

    @Schema(description = "User's email address", example = "john@example.com")
    private String email;

    @Schema(description = "Assigned role", example = "ADMIN")
    private Role role;
}
