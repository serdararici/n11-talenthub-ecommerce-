package com.n11.talenthub.product.config;

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
                title = "Product Service API",
                version = "1.0.0",
                description = "Product catalog: listing, search, category filtering, and admin CRUD",
                contact = @Contact(name = "N11 TalentHub", email = "serdararici3@gmail.com")
        ),
        servers = @Server(url = "http://localhost:8082", description = "Local development")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Required for admin endpoints (POST, PUT, DELETE)"
)
public class OpenApiConfig {
}
