package com.inventra.dto.response;

import com.inventra.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    @Schema(description = "User's unique identifier", example = "1")
    private Long id;

    @Schema(description = "User's full name", example = "John Doe")
    private String name;

    @Schema(description = "User's email address", example = "john@example.com")
    private String email;

    @Schema(description = "Assigned role", example = "ADMIN")
    private Role role;

    @Schema(description = "Account creation timestamp", example = "2026-01-15T10:30:00")
    private LocalDateTime createdAt;
}
