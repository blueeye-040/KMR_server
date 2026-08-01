package com.kmr.marketplace.repository;

import com.kmr.marketplace.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT c FROM CartItem c " +
           "JOIN FETCH c.shopProduct sp " +
           "JOIN FETCH sp.shop " +
           "JOIN FETCH sp.product p " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE c.user.id = :uid")
    List<CartItem> findByUserIdWithDetails(@Param("uid") Long userId);

    Optional<CartItem> findByUserIdAndShopProductId(Long userId, Long shopProductId);

    int countByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.id = :uid")
    void deleteByUserId(@Param("uid") Long userId);
}
