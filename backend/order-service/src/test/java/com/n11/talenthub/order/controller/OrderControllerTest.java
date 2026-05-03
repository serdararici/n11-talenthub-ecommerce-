package com.n11.talenthub.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11.talenthub.order.config.SecurityConfig;
import com.n11.talenthub.order.dto.*;
import com.n11.talenthub.order.entity.OrderStatus;
import com.n11.talenthub.order.exception.OrderNotFoundException;
import com.n11.talenthub.order.security.OrderPrincipal;
import com.n11.talenthub.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = OrderController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityConfig.class
        )
)
@Import(OrderControllerTest.TestSecurityConfig.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private OrderService orderService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.PUT, "/api/orders/*/status").hasAuthority("ROLE_ADMIN")
                            .anyRequest().authenticated());
            return http.build();
        }
    }

    private static UsernamePasswordAuthenticationToken userAuth() {
        OrderPrincipal principal = new OrderPrincipal(1L, "user@example.com", "ROLE_USER");
        return new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private static UsernamePasswordAuthenticationToken adminAuth() {
        OrderPrincipal principal = new OrderPrincipal(99L, "admin@example.com", "ROLE_ADMIN");
        return new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private OrderResponse sampleOrderResponse() {
        OrderItemResponse item = OrderItemResponse.builder()
                .id(1L).productId(10L).productName("Laptop").brand("Apple")
                .price(new BigDecimal("5000")).quantity(2)
                .subtotal(new BigDecimal("10000")).build();
        return OrderResponse.builder()
                .id(1L).userId(1L).userEmail("user@example.com")
                .shippingAddress("Istanbul").status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("10000")).paymentMethod("CREDIT_CARD")
                .items(List.of(item)).build();
    }

    @Test
    void createOrder_authenticated_returns201() throws Exception {
        OrderItemRequest itemReq = OrderItemRequest.builder()
                .productId(10L).productName("Laptop").brand("Apple")
                .price(new BigDecimal("5000")).quantity(2).build();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingAddress("Istanbul").paymentMethod("CREDIT_CARD")
                .items(List.of(itemReq)).build();

        when(orderService.createOrder(any(OrderPrincipal.class), any(CreateOrderRequest.class)))
                .thenReturn(sampleOrderResponse());

        mockMvc.perform(post("/api/orders")
                        .with(authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(10000));
    }

    @Test
    void createOrder_unauthenticated_returns403() throws Exception {
        OrderItemRequest itemReq = OrderItemRequest.builder()
                .productId(10L).productName("Laptop").price(new BigDecimal("5000")).quantity(1).build();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .shippingAddress("Istanbul").items(List.of(itemReq)).build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createOrder_missingShippingAddress_returns400() throws Exception {
        OrderItemRequest itemReq = OrderItemRequest.builder()
                .productId(10L).productName("Laptop").price(new BigDecimal("5000")).quantity(1).build();
        CreateOrderRequest request = CreateOrderRequest.builder()
                .items(List.of(itemReq)).build(); // no shippingAddress

        mockMvc.perform(post("/api/orders")
                        .with(authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyOrders_returns200() throws Exception {
        PageResponse<OrderResponse> page = PageResponse.<OrderResponse>builder()
                .content(List.of(sampleOrderResponse())).pageNumber(0).pageSize(10)
                .totalElements(1).totalPages(1).last(true).build();
        when(orderService.getOrdersByUser(eq(1L), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/orders").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void getOrderById_owner_returns200() throws Exception {
        when(orderService.getOrderById(eq(1L), any(OrderPrincipal.class)))
                .thenReturn(sampleOrderResponse());

        mockMvc.perform(get("/api/orders/1").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getOrderById_unknownId_returns404() throws Exception {
        when(orderService.getOrderById(eq(99L), any(OrderPrincipal.class)))
                .thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/api/orders/99").with(authentication(userAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_asAdmin_returns200() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(OrderStatus.CONFIRMED);

        OrderResponse confirmed = sampleOrderResponse();
        confirmed.setStatus(OrderStatus.CONFIRMED);
        when(orderService.updateStatus(eq(1L), eq(OrderStatus.CONFIRMED))).thenReturn(confirmed);

        mockMvc.perform(put("/api/orders/1/status")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void updateStatus_asUser_returns403() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(OrderStatus.CONFIRMED);

        mockMvc.perform(put("/api/orders/1/status")
                        .with(authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
