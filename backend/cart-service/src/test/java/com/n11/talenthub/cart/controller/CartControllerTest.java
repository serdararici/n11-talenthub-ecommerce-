package com.n11.talenthub.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11.talenthub.cart.config.SecurityConfig;
import com.n11.talenthub.cart.dto.AddItemRequest;
import com.n11.talenthub.cart.dto.CartItemDto;
import com.n11.talenthub.cart.dto.CartResponse;
import com.n11.talenthub.cart.dto.UpdateItemRequest;
import com.n11.talenthub.cart.security.CartPrincipal;
import com.n11.talenthub.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = CartController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityConfig.class
        )
)
@Import(CartControllerTest.TestSecurityConfig.class)
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CartService cartService;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            return http.build();
        }
    }

    private static UsernamePasswordAuthenticationToken userAuth() {
        CartPrincipal principal = new CartPrincipal(1L, "user@example.com", "ROLE_USER");
        return new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private CartResponse emptyCart() {
        return CartResponse.of(1L, List.of());
    }

    private CartResponse cartWithItem() {
        CartItemDto item = CartItemDto.builder()
                .productId(10L).productName("Laptop").brand("Apple")
                .price(new BigDecimal("5000")).quantity(1).build();
        return CartResponse.of(1L, List.of(item));
    }

    @Test
    void getCart_authenticated_returns200() throws Exception {
        when(cartService.getCart(1L)).thenReturn(emptyCart());

        mockMvc.perform(get("/api/cart").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.totalItems").value(0));
    }

    @Test
    void getCart_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isForbidden());
    }

    @Test
    void addItem_validRequest_returns200() throws Exception {
        AddItemRequest request = AddItemRequest.builder()
                .productId(10L).productName("Laptop").brand("Apple")
                .category("Bilgisayar").price(new BigDecimal("5000")).quantity(1).build();

        when(cartService.addItem(eq(1L), any(AddItemRequest.class))).thenReturn(cartWithItem());

        mockMvc.perform(post("/api/cart/items")
                        .with(authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.items[0].productName").value("Laptop"));
    }

    @Test
    void addItem_missingProductId_returns400() throws Exception {
        AddItemRequest request = AddItemRequest.builder()
                .productName("Laptop").price(new BigDecimal("5000")).quantity(1).build();

        mockMvc.perform(post("/api/cart/items")
                        .with(authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_validQuantity_returns200() throws Exception {
        UpdateItemRequest request = new UpdateItemRequest();
        request.setQuantity(3);

        when(cartService.updateItem(eq(1L), eq(10L), eq(3))).thenReturn(cartWithItem());

        mockMvc.perform(put("/api/cart/items/10")
                        .with(authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void removeItem_returns200() throws Exception {
        when(cartService.removeItem(eq(1L), eq(10L))).thenReturn(emptyCart());

        mockMvc.perform(delete("/api/cart/items/10").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(0));
    }

    @Test
    void clearCart_returns200() throws Exception {
        doNothing().when(cartService).clearCart(1L);

        mockMvc.perform(delete("/api/cart").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
