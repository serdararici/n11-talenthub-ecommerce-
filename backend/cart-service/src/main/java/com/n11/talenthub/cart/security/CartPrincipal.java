package com.n11.talenthub.cart.security;

public record CartPrincipal(Long userId, String email, String role) {
}
