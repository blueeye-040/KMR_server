package com.kmr.marketplace.repository;

import com.kmr.marketplace.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Home feed sections — top 10 per section, @EntityGraph avoids N+1 on brand+category
    @EntityGraph(attributePaths = {"brand", "category"})
    List<Product> findTop10ByFeaturedTrueAndActiveTrueOrderByIdDesc();

    @EntityGraph(attributePaths = {"brand", "category"})
    List<Product> findTop10ByNewArrivalTrueAndActiveTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"brand", "category"})
    List<Product> findTop10ByTopSellingTrueAndActiveTrueOrderByIdDesc();

    // Paginated products for categories page
    @EntityGraph(attributePaths = {"brand", "category"})
    Page<Product> findByCategoryIdAndActiveTrueOrderByIdDesc(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"brand", "category"})
    Page<Product> findByActiveTrueOrderByIdDesc(Pageable pageable);

    // Single product with brand + category (used by product detail page)
    @EntityGraph(attributePaths = {"brand", "category"})
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.active = true")
    java.util.Optional<Product> findActiveById(@Param("id") Long id);

    // Related products: same category, different id, active
    @EntityGraph(attributePaths = {"brand", "category"})
    @Query("SELECT p FROM Product p WHERE p.category.id = :catId AND p.id <> :excludeId AND p.active = true ORDER BY p.id DESC")
    java.util.List<Product> findRelated(
            @Param("catId") Long categoryId,
            @Param("excludeId") Long excludeId,
            org.springframework.data.domain.Pageable pageable);

    // Analytics counts
    long countByActiveTrue();
    long countByCategoryIdAndActiveTrue(Long categoryId);
}
