package com.n11.talenthub.product.repository;

import com.n11.talenthub.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndActiveTrue(Long id);

    @Query(value = """
            SELECT * FROM products
            WHERE active = true
              AND (CAST(:category AS text) IS NULL OR category = CAST(:category AS text))
              AND (CAST(:search AS text) IS NULL
                   OR LOWER(name) LIKE LOWER('%' || CAST(:search AS text) || '%')
                   OR LOWER(description) LIKE LOWER('%' || CAST(:search AS text) || '%'))
            ORDER BY created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM products
            WHERE active = true
              AND (CAST(:category AS text) IS NULL OR category = CAST(:category AS text))
              AND (CAST(:search AS text) IS NULL
                   OR LOWER(name) LIKE LOWER('%' || CAST(:search AS text) || '%')
                   OR LOWER(description) LIKE LOWER('%' || CAST(:search AS text) || '%'))
            """,
            nativeQuery = true)
    Page<Product> findByFilters(
            @Param("category") String category,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.active = true ORDER BY p.category")
    List<String> findAllActiveCategories();
}
