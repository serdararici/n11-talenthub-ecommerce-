package com.n11.talenthub.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11.talenthub.product.config.SecurityConfig;
import com.n11.talenthub.product.dto.PageResponse;
import com.n11.talenthub.product.security.JwtAuthFilter;
import com.n11.talenthub.product.dto.ProductRequest;
import com.n11.talenthub.product.dto.ProductResponse;
import com.n11.talenthub.product.exception.ProductNotFoundException;
import com.n11.talenthub.product.service.ProductService;
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = ProductController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityConfig.class
        )
)
@Import(ProductControllerTest.TestSecurityConfig.class)
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ProductService productService;
    @MockBean private JwtAuthFilter jwtAuthFilter;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                            .anyRequest().authenticated());
            return http.build();
        }
    }

    private ProductResponse sampleResponse() {
        return ProductResponse.builder()
                .id(1L).name("Test Laptop").brand("Apple").category("Bilgisayar")
                .price(new BigDecimal("10000")).originalPrice(new BigDecimal("12000"))
                .stockQuantity(5).freeShipping(true).rating(4.5).reviewCount(100)
                .active(true).build();
    }

    @Test
    void getProducts_noAuth_returns200() throws Exception {
        PageResponse<ProductResponse> page = PageResponse.<ProductResponse>builder()
                .content(List.of(sampleResponse())).pageNumber(0).pageSize(10)
                .totalElements(1).totalPages(1).last(true).build();
        when(productService.getProducts(anyInt(), anyInt(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Laptop"));
    }

    @Test
    void getProducts_withCategoryParam_returns200() throws Exception {
        PageResponse<ProductResponse> page = PageResponse.<ProductResponse>builder()
                .content(List.of(sampleResponse())).pageNumber(0).pageSize(10)
                .totalElements(1).totalPages(1).last(true).build();
        when(productService.getProducts(0, 10, "Bilgisayar", null)).thenReturn(page);

        mockMvc.perform(get("/api/products").param("category", "Bilgisayar"))
                .andExpect(status().isOk());
    }

    @Test
    void getProductById_existingId_returns200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.brand").value("Apple"));
    }

    @Test
    void getProductById_unknownId_returns404() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ProductNotFoundException("Product not found with id: 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCategories_returns200() throws Exception {
        when(productService.getCategories()).thenReturn(List.of("Bilgisayar", "Elektronik", "Moda"));

        mockMvc.perform(get("/api/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("Bilgisayar"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_asAdmin_returns201() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("New Laptop");
        request.setBrand("Dell");
        request.setCategory("Bilgisayar");
        request.setPrice(new BigDecimal("8000"));
        request.setStockQuantity(5);

        when(productService.createProduct(any(ProductRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createProduct_unauthenticated_returns403() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("New Laptop");
        request.setCategory("Bilgisayar");
        request.setPrice(new BigDecimal("8000"));
        request.setStockQuantity(5);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_asAdmin_returns200() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Updated Laptop");
        request.setBrand("Dell");
        request.setCategory("Bilgisayar");
        request.setPrice(new BigDecimal("9000"));
        request.setStockQuantity(3);

        when(productService.updateProduct(eq(1L), any(ProductRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_asAdmin_returns200() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_missingRequiredFields_returns400() throws Exception {
        ProductRequest request = new ProductRequest();
        // name, category, price, stockQuantity are required but not set

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
