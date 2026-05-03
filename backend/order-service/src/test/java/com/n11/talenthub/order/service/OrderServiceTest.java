package com.n11.talenthub.order.service;

import com.n11.talenthub.order.dto.*;
import com.n11.talenthub.order.entity.Order;
import com.n11.talenthub.order.entity.OrderItem;
import com.n11.talenthub.order.entity.OrderStatus;
import com.n11.talenthub.order.exception.OrderNotFoundException;
import com.n11.talenthub.order.repository.OrderRepository;
import com.n11.talenthub.order.security.OrderPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @InjectMocks private OrderService orderService;

    private Order sampleOrder;
    private OrderPrincipal ownerPrincipal;
    private OrderPrincipal adminPrincipal;
    private OrderPrincipal otherUserPrincipal;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        ownerPrincipal = new OrderPrincipal(1L, "owner@example.com", "ROLE_USER");
        adminPrincipal = new OrderPrincipal(99L, "admin@example.com", "ROLE_ADMIN");
        otherUserPrincipal = new OrderPrincipal(2L, "other@example.com", "ROLE_USER");

        OrderItem item = OrderItem.builder()
                .id(1L).productId(10L).productName("Laptop")
                .brand("Apple").price(new BigDecimal("5000")).quantity(2).build();

        sampleOrder = Order.builder()
                .id(1L).userId(1L).userEmail("owner@example.com")
                .shippingAddress("Istanbul, Turkey").status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("10000")).paymentMethod("CREDIT_CARD")
                .items(new ArrayList<>(List.of(item))).build();

        item.setOrder(sampleOrder);

        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(10L).productName("Laptop").brand("Apple")
                .price(new BigDecimal("5000")).quantity(2).build();

        createOrderRequest = CreateOrderRequest.builder()
                .shippingAddress("Istanbul, Turkey")
                .paymentMethod("CREDIT_CARD")
                .items(List.of(itemRequest))
                .build();
    }

    @Test
    void createOrder_calculatesTotalAndSaves() {
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        OrderResponse response = orderService.createOrder(ownerPrincipal, createOrderRequest);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(response.getItems()).hasSize(1);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_totalAmount_isSumOfItemSubtotals() {
        Order saved = Order.builder()
                .id(2L).userId(1L).userEmail("owner@example.com")
                .shippingAddress("Istanbul").status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("10000")).items(new ArrayList<>()).build();
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        OrderResponse response = orderService.createOrder(ownerPrincipal, createOrderRequest);

        assertThat(response.getTotalAmount()).isEqualByComparingTo(
                new BigDecimal("5000").multiply(BigDecimal.valueOf(2)));
    }

    @Test
    void getOrdersByUser_returnsPaginatedOrders() {
        var page = new PageImpl<>(List.of(sampleOrder), PageRequest.of(0, 10), 1);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class))).thenReturn(page);

        PageResponse<OrderResponse> result = orderService.getOrdersByUser(1L, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getOrderById_owner_returnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        OrderResponse response = orderService.getOrderById(1L, ownerPrincipal);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getOrderById_admin_canAccessAnyOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        OrderResponse response = orderService.getOrderById(1L, adminPrincipal);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getOrderById_otherUser_throwsAccessDenied() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.getOrderById(1L, otherUserPrincipal))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getOrderById_unknownId_throwsOrderNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L, ownerPrincipal))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void updateStatus_existingOrder_changesStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        OrderResponse response = orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        assertThat(sampleOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository).save(sampleOrder);
    }

    @Test
    void updateStatus_unknownId_throwsOrderNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(99L, OrderStatus.CONFIRMED))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
