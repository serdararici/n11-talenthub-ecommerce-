package com.n11.talenthub.cart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.n11.talenthub.cart.dto.AddItemRequest;
import com.n11.talenthub.cart.dto.CartItemDto;
import com.n11.talenthub.cart.dto.CartResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private HashOperations<String, Object, Object> hashOps;

    private CartService cartService;
    private ObjectMapper objectMapper;

    private static final Long USER_ID = 1L;
    private static final String CART_KEY = "cart:1";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        cartService = new CartService(redisTemplate, objectMapper);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
    }

    @Test
    void getCart_emptyCart_returnsEmptyResponse() {
        when(hashOps.entries(CART_KEY)).thenReturn(new HashMap<>());

        CartResponse response = cartService.getCart(USER_ID);

        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getItems()).isEmpty();
        assertThat(response.getTotalItems()).isZero();
        assertThat(response.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void addItem_newProduct_addsItemToCart() throws Exception {
        AddItemRequest request = AddItemRequest.builder()
                .productId(10L).productName("Laptop").brand("Apple")
                .category("Bilgisayar").price(new BigDecimal("5000")).quantity(1).build();

        when(hashOps.get(CART_KEY, "10")).thenReturn(null);
        when(hashOps.entries(CART_KEY)).thenReturn(new HashMap<>());

        cartService.addItem(USER_ID, request);

        verify(hashOps).put(eq(CART_KEY), eq("10"), anyString());
    }

    @Test
    void addItem_existingProduct_incrementsQuantity() throws Exception {
        CartItemDto existing = CartItemDto.builder()
                .productId(10L).productName("Laptop").price(new BigDecimal("5000")).quantity(2).build();
        String existingJson = objectMapper.writeValueAsString(existing);

        AddItemRequest request = AddItemRequest.builder()
                .productId(10L).productName("Laptop").price(new BigDecimal("5000")).quantity(3).build();

        when(hashOps.get(CART_KEY, "10")).thenReturn(existingJson);
        when(hashOps.entries(CART_KEY)).thenReturn(new HashMap<>());

        cartService.addItem(USER_ID, request);

        verify(hashOps).put(eq(CART_KEY), eq("10"), argThat(json -> {
            try {
                CartItemDto updated = objectMapper.readValue((String) json, CartItemDto.class);
                return updated.getQuantity() == 5;
            } catch (Exception e) {
                return false;
            }
        }));
    }

    @Test
    void updateItem_positiveQuantity_updatesItem() throws Exception {
        CartItemDto existing = CartItemDto.builder()
                .productId(10L).productName("Laptop").price(new BigDecimal("5000")).quantity(1).build();
        String existingJson = objectMapper.writeValueAsString(existing);

        when(hashOps.get(CART_KEY, "10")).thenReturn(existingJson);
        when(hashOps.entries(CART_KEY)).thenReturn(new HashMap<>());

        cartService.updateItem(USER_ID, 10L, 5);

        verify(hashOps).put(eq(CART_KEY), eq("10"), anyString());
    }

    @Test
    void updateItem_zeroQuantity_removesItem() throws Exception {
        when(hashOps.entries(CART_KEY)).thenReturn(new HashMap<>());

        cartService.updateItem(USER_ID, 10L, 0);

        verify(hashOps).delete(CART_KEY, "10");
        verify(hashOps, never()).put(any(), any(), any());
    }

    @Test
    void updateItem_itemNotInCart_throwsIllegalArgument() {
        when(hashOps.get(CART_KEY, "99")).thenReturn(null);

        assertThatThrownBy(() -> cartService.updateItem(USER_ID, 99L, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void removeItem_deletesHashField() {
        when(hashOps.entries(CART_KEY)).thenReturn(new HashMap<>());

        cartService.removeItem(USER_ID, 10L);

        verify(hashOps).delete(CART_KEY, "10");
    }

    @Test
    void clearCart_deletesEntireKey() {
        cartService.clearCart(USER_ID);

        verify(redisTemplate).delete(CART_KEY);
    }

    @Test
    void getCart_withItems_calculatesTotalsCorrectly() throws Exception {
        CartItemDto item1 = CartItemDto.builder()
                .productId(1L).productName("A").price(new BigDecimal("100")).quantity(2).build();
        CartItemDto item2 = CartItemDto.builder()
                .productId(2L).productName("B").price(new BigDecimal("50")).quantity(3).build();

        Map<Object, Object> entries = new HashMap<>();
        entries.put("1", objectMapper.writeValueAsString(item1));
        entries.put("2", objectMapper.writeValueAsString(item2));
        when(hashOps.entries(CART_KEY)).thenReturn(entries);

        CartResponse response = cartService.getCart(USER_ID);

        assertThat(response.getTotalItems()).isEqualTo(5);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("350"));
    }
}
