package com.n11.talenthub.cart.controller;

import com.n11.talenthub.cart.dto.AddItemRequest;
import com.n11.talenthub.cart.dto.ApiResponse;
import com.n11.talenthub.cart.dto.CartResponse;
import com.n11.talenthub.cart.dto.UpdateItemRequest;
import com.n11.talenthub.cart.security.CartPrincipal;
import com.n11.talenthub.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart", description = "Shopping cart management")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal CartPrincipal principal) {
        CartResponse cart = cartService.getCart(principal.userId());
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart retrieved successfully"));
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the cart (increments quantity if already present)")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal CartPrincipal principal,
            @Valid @RequestBody AddItemRequest request) {
        CartResponse cart = cartService.addItem(principal.userId(), request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart"));
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Update quantity of a cart item (quantity=0 removes the item)")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @AuthenticationPrincipal CartPrincipal principal,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateItemRequest request) {
        CartResponse cart = cartService.updateItem(principal.userId(), productId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart item updated"));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove a specific item from the cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal CartPrincipal principal,
            @PathVariable Long productId) {
        CartResponse cart = cartService.removeItem(principal.userId(), productId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart"));
    }

    @DeleteMapping
    @Operation(summary = "Clear the entire cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal CartPrincipal principal) {
        cartService.clearCart(principal.userId());
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared"));
    }
}
