package com.n11.talenthub.order.security;

public record OrderPrincipal(Long userId, String email, String role) {
}
