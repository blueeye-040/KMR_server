package com.kmr.marketplace.repository;

import com.kmr.marketplace.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.product.id = :pid ORDER BY r.createdAt DESC")
    Page<Review> findByProductId(@Param("pid") Long productId, Pageable pageable);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :pid GROUP BY r.rating")
    List<Object[]> ratingBreakdown(@Param("pid") Long productId);

    @Query("SELECT COALESCE(AVG(CAST(r.rating AS double)), 0.0), COUNT(r) FROM Review r WHERE r.product.id = :pid")
    Object[] avgAndCount(@Param("pid") Long productId);

    boolean existsByProductIdAndUserId(Long productId, Long userId);
}
