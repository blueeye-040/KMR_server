package com.kmr.marketplace.repository;

import com.kmr.marketplace.entity.ShopProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopProductRepository extends JpaRepository<ShopProduct, Long> {

    // Single batch query — fetches best prices for many products at once (no N+1)
    @Query("SELECT sp FROM ShopProduct sp JOIN FETCH sp.shop " +
           "WHERE sp.product.id IN :ids AND sp.available = true " +
           "ORDER BY sp.sellingPrice ASC")
    List<ShopProduct> findByProductIds(@Param("ids") List<Long> ids);

    // All sellers for a single product — JOIN FETCH shop to avoid N+1
    @Query("SELECT sp FROM ShopProduct sp JOIN FETCH sp.shop " +
           "WHERE sp.product.id = :pid AND sp.available = true " +
           "ORDER BY sp.sellingPrice ASC")
    List<ShopProduct> findByProductIdAndAvailableTrueOrderBySellingPriceAsc(@Param("pid") Long productId);
}
