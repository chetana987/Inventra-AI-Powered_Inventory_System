package com.inventra.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeRequest {

    @Schema(description = "Current password for verification", example = "currentPass1")
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Schema(description = "New password (minimum 6 characters)", example = "newSecurePass1")
    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String newPassword;
}
