package com.n11.talenthub.cart.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Cart Service API",
                version = "1.0.0",
                description = "Redis-backed shopping cart: add, update quantity, remove items and clear cart",
                contact = @Contact(name = "N11 TalentHub", email = "serdararici3@gmail.com")
        ),
        servers = @Server(url = "http://localhost:8083", description = "Local development")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "All cart endpoints require authentication"
)
public class OpenApiConfig {
}
