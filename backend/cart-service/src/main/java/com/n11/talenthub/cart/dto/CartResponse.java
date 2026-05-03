package com.n11.talenthub.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private Long userId;
    private List<CartItemDto> items;
    private int totalItems;
    private BigDecimal totalPrice;

    public static CartResponse of(Long userId, List<CartItemDto> items) {
        int totalItems = items.stream().mapToInt(CartItemDto::getQuantity).sum();
        BigDecimal totalPrice = items.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponse.builder()
                .userId(userId)
                .items(items)
                .totalItems(totalItems)
                .totalPrice(totalPrice)
                .build();
    }
}
