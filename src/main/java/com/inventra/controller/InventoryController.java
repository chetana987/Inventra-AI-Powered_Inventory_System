package com.inventra.controller;

import com.inventra.dto.request.InventoryRequest;
import com.inventra.dto.response.ApiResponse;
import com.inventra.dto.response.InventoryResponse;
import com.inventra.dto.response.PageResponse;
import com.inventra.entity.User;
import com.inventra.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Add stock", description = "Admin only. Increases product quantity and records an STOCK_IN transaction. Uses pessimistic locking to prevent race conditions.")
    @PostMapping("/stock-in")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> stockIn(
            @Valid @RequestBody InventoryRequest request,
            @AuthenticationPrincipal User performedBy) {
        InventoryResponse response = inventoryService.stockIn(request, performedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @Operation(summary = "Remove stock", description = "Admin only. Decreases product quantity (prevents negative stock) and records an STOCK_OUT transaction. Uses pessimistic locking.")
    @PostMapping("/stock-out")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> stockOut(
            @Valid @RequestBody InventoryRequest request,
            @AuthenticationPrincipal User performedBy) {
        InventoryResponse response = inventoryService.stockOut(request, performedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @Operation(summary = "Get transaction history", description = "Returns paginated inventory transaction history, optionally filtered by product ID.")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<InventoryResponse>>> getHistory(
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<InventoryResponse> response =
                inventoryService.getHistory(productId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
