package com.n11.talenthub.product.service;

import com.n11.talenthub.product.dto.PageResponse;
import com.n11.talenthub.product.dto.ProductRequest;
import com.n11.talenthub.product.dto.ProductResponse;
import com.n11.talenthub.product.entity.Product;
import com.n11.talenthub.product.exception.ProductNotFoundException;
import com.n11.talenthub.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @InjectMocks private ProductService productService;

    private Product sampleProduct;
    private ProductRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Test Laptop")
                .brand("TestBrand")
                .category("Bilgisayar")
                .price(new BigDecimal("10000"))
                .originalPrice(new BigDecimal("12000"))
                .stockQuantity(10)
                .freeShipping(true)
                .rating(4.5)
                .reviewCount(100)
                .active(true)
                .build();

        sampleRequest = new ProductRequest();
        sampleRequest.setName("Test Laptop");
        sampleRequest.setBrand("TestBrand");
        sampleRequest.setCategory("Bilgisayar");
        sampleRequest.setPrice(new BigDecimal("10000"));
        sampleRequest.setOriginalPrice(new BigDecimal("12000"));
        sampleRequest.setStockQuantity(10);
        sampleRequest.setFreeShipping(true);
        sampleRequest.setRating(4.5);
        sampleRequest.setReviewCount(100);
    }

    @Test
    void getProducts_returnsPageResponse() {
        var page = new PageImpl<>(List.of(sampleProduct), PageRequest.of(0, 10), 1);
        when(productRepository.findByFilters(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        PageResponse<ProductResponse> result = productService.getProducts(0, 10, null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Laptop");
    }

    @Test
    void getProducts_withCategoryFilter_passesFilterToRepository() {
        var page = new PageImpl<>(List.of(sampleProduct), PageRequest.of(0, 10), 1);
        when(productRepository.findByFilters(eq("Bilgisayar"), isNull(), any(Pageable.class))).thenReturn(page);

        PageResponse<ProductResponse> result = productService.getProducts(0, 10, "Bilgisayar", null);

        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findByFilters(eq("Bilgisayar"), isNull(), any(Pageable.class));
    }

    @Test
    void getProductById_existingId_returnsProduct() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Laptop");
        assertThat(response.getBrand()).isEqualTo("TestBrand");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("10000"));
    }

    @Test
    void getProductById_unknownId_throwsProductNotFoundException() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createProduct_savesAndReturnsProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.createProduct(sampleRequest);

        assertThat(response.getName()).isEqualTo("Test Laptop");
        assertThat(response.getCategory()).isEqualTo("Bilgisayar");
        assertThat(response.isFreeShipping()).isTrue();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_existingId_updatesAndReturns() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        sampleRequest.setName("Updated Laptop");
        sampleRequest.setPrice(new BigDecimal("9000"));

        ProductResponse response = productService.updateProduct(1L, sampleRequest);

        assertThat(response).isNotNull();
        verify(productRepository).save(sampleProduct);
    }

    @Test
    void updateProduct_unknownId_throwsProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, sampleRequest))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void deleteProduct_existingId_softDeletes() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        productService.deleteProduct(1L);

        assertThat(sampleProduct.isActive()).isFalse();
        verify(productRepository).save(sampleProduct);
    }

    @Test
    void deleteProduct_unknownId_throwsProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getCategories_returnsDistinctList() {
        when(productRepository.findAllActiveCategories())
                .thenReturn(List.of("Bilgisayar", "Elektronik", "Moda"));

        List<String> categories = productService.getCategories();

        assertThat(categories).containsExactly("Bilgisayar", "Elektronik", "Moda");
    }
}
