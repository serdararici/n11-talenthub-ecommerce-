package com.n11.talenthub.product.service;

import com.n11.talenthub.product.dto.PageResponse;
import com.n11.talenthub.product.dto.ProductRequest;
import com.n11.talenthub.product.dto.ProductResponse;
import com.n11.talenthub.product.entity.Product;
import com.n11.talenthub.product.exception.ProductNotFoundException;
import com.n11.talenthub.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(int page, int size, String category, String search) {
        Pageable pageable = PageRequest.of(page, size);
        String categoryFilter = (category != null && !category.isBlank()) ? category : null;
        String searchFilter = (search != null && !search.isBlank()) ? search : null;
        Page<Product> productPage = productRepository.findByFilters(categoryFilter, searchFilter, pageable);
        return toPageResponse(productPage);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return toResponse(product);
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .description(request.getDescription())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .badge(request.getBadge())
                .freeShipping(request.isFreeShipping())
                .rating(request.getRating())
                .reviewCount(request.getReviewCount())
                .build();
        product = productRepository.save(product);
        log.info("Product created: id={}, name={}", product.getId(), product.getName());
        return toResponse(product);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        product.setBadge(request.getBadge());
        product.setFreeShipping(request.isFreeShipping());
        product.setRating(request.getRating());
        product.setReviewCount(request.getReviewCount());
        product = productRepository.save(product);
        log.info("Product updated: id={}", product.getId());
        return toResponse(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted: id={}", id);
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return productRepository.findAllActiveCategories();
    }

    private PageResponse<ProductResponse> toPageResponse(Page<Product> page) {
        List<ProductResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return PageResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .description(product.getDescription())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .badge(product.getBadge())
                .freeShipping(product.isFreeShipping())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
