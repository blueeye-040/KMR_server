package com.kmr.marketplace.repository;

import com.kmr.marketplace.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    @Query("SELECT w FROM WishlistItem w " +
           "JOIN FETCH w.product p " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE w.user.id = :uid ORDER BY w.id DESC")
    List<WishlistItem> findByUserIdWithProduct(@Param("uid") Long userId);

    Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    int countByUserId(Long userId);
}
