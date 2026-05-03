package com.n11.talenthub.cart.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11.talenthub.cart.dto.AddItemRequest;
import com.n11.talenthub.cart.dto.CartItemDto;
import com.n11.talenthub.cart.dto.CartResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {

    private static final String CART_KEY_PREFIX = "cart:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private String cartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    public CartResponse getCart(Long userId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey(userId));
        List<CartItemDto> items = deserializeItems(entries);
        log.debug("Get cart for user {}: {} items", userId, items.size());
        return CartResponse.of(userId, items);
    }

    public CartResponse addItem(Long userId, AddItemRequest request) {
        String key = cartKey(userId);
        String field = String.valueOf(request.getProductId());
        String existing = (String) redisTemplate.opsForHash().get(key, field);

        CartItemDto item;
        if (existing != null) {
            item = deserializeItem(existing);
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            item = CartItemDto.builder()
                    .productId(request.getProductId())
                    .productName(request.getProductName())
                    .brand(request.getBrand())
                    .category(request.getCategory())
                    .price(request.getPrice())
                    .imageUrl(request.getImageUrl())
                    .quantity(request.getQuantity())
                    .build();
        }

        redisTemplate.opsForHash().put(key, field, serializeItem(item));
        log.info("Added item productId={} qty={} to cart for user {}", request.getProductId(), request.getQuantity(), userId);
        return getCart(userId);
    }

    public CartResponse updateItem(Long userId, Long productId, Integer quantity) {
        String key = cartKey(userId);
        String field = String.valueOf(productId);

        if (quantity <= 0) {
            redisTemplate.opsForHash().delete(key, field);
            log.info("Removed item productId={} from cart for user {}", productId, userId);
        } else {
            String existing = (String) redisTemplate.opsForHash().get(key, field);
            if (existing == null) {
                throw new IllegalArgumentException("Item not found in cart: productId=" + productId);
            }
            CartItemDto item = deserializeItem(existing);
            item.setQuantity(quantity);
            redisTemplate.opsForHash().put(key, field, serializeItem(item));
            log.info("Updated item productId={} qty={} in cart for user {}", productId, quantity, userId);
        }
        return getCart(userId);
    }

    public CartResponse removeItem(Long userId, Long productId) {
        redisTemplate.opsForHash().delete(cartKey(userId), String.valueOf(productId));
        log.info("Removed item productId={} from cart for user {}", productId, userId);
        return getCart(userId);
    }

    public void clearCart(Long userId) {
        redisTemplate.delete(cartKey(userId));
        log.info("Cleared cart for user {}", userId);
    }

    private String serializeItem(CartItemDto item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize cart item", e);
        }
    }

    private CartItemDto deserializeItem(String json) {
        try {
            return objectMapper.readValue(json, CartItemDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize cart item", e);
        }
    }

    private List<CartItemDto> deserializeItems(Map<Object, Object> entries) {
        List<CartItemDto> items = new ArrayList<>();
        for (Object value : entries.values()) {
            try {
                items.add(objectMapper.readValue((String) value, CartItemDto.class));
            } catch (JsonProcessingException e) {
                log.warn("Skipping malformed cart item: {}", e.getMessage());
            }
        }
        return items;
    }
}
