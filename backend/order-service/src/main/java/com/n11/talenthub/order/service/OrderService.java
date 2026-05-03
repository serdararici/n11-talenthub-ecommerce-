package com.n11.talenthub.order.service;

import com.n11.talenthub.order.dto.*;
import com.n11.talenthub.order.entity.Order;
import com.n11.talenthub.order.entity.OrderItem;
import com.n11.talenthub.order.entity.OrderStatus;
import com.n11.talenthub.order.exception.OrderNotFoundException;
import com.n11.talenthub.order.repository.OrderRepository;
import com.n11.talenthub.order.security.OrderPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderResponse createOrder(OrderPrincipal principal, CreateOrderRequest request) {
        BigDecimal totalAmount = request.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .userId(principal.userId())
                .userEmail(principal.email())
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(i -> OrderItem.builder()
                        .order(order)
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .brand(i.getBrand())
                        .price(i.getPrice())
                        .quantity(i.getQuantity())
                        .build())
                .collect(Collectors.toList());

        order.setItems(items);
        Order saved = orderRepository.save(order);
        log.info("Order created: id={}, userId={}, total={}", saved.getId(), saved.getUserId(), saved.getTotalAmount());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByUser(Long userId, int page, int size) {
        Page<Order> orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size));
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return PageResponse.<OrderResponse>builder()
                .content(content)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, OrderPrincipal principal) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getUserId().equals(principal.userId()) && !"ROLE_ADMIN".equals(principal.role())) {
            throw new AccessDeniedException("Not your order");
        }
        return toResponse(order);
    }

    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        log.info("Order {} status updated to {}", orderId, newStatus);
        return toResponse(saved);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .brand(i.getBrand())
                        .price(i.getPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
