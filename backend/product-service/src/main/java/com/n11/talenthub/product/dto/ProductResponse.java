package com.n11.talenthub.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String brand;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stockQuantity;
    private String category;
    private String imageUrl;
    private String badge;
    private boolean freeShipping;
    private Double rating;
    private Integer reviewCount;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
