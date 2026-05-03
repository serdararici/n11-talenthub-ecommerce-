package com.n11.talenthub.order.controller;

import com.n11.talenthub.order.dto.*;
import com.n11.talenthub.order.security.OrderPrincipal;
import com.n11.talenthub.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal OrderPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Order created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get current user's orders (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal OrderPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<OrderResponse> orders = orderService.getOrdersByUser(principal.userId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single order by ID (owner or admin)")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @AuthenticationPrincipal OrderPrincipal principal,
            @PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id, principal);
        return ResponseEntity.ok(ApiResponse.success(order, "Order retrieved successfully"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update order status (ADMIN only)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        OrderResponse order = orderService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(order, "Order status updated"));
    }
}
